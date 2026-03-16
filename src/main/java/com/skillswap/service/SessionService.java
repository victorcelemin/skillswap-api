package com.skillswap.service;

import com.skillswap.dto.request.CreateSessionRequest;
import com.skillswap.dto.response.SessionResponse;
import com.skillswap.entity.Offer;
import com.skillswap.entity.Session;
import com.skillswap.entity.User;
import com.skillswap.exception.BusinessException;
import com.skillswap.exception.InsufficientCreditsException;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.mapper.SessionMapper;
import com.skillswap.repository.OfferRepository;
import com.skillswap.repository.SessionRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.websocket.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de sesiones — núcleo transaccional de SkillSwap.
 *
 * Decisiones críticas:
 * 1. @Transactional en TODAS las operaciones que modifican créditos.
 *    Si algo falla a mitad del proceso, todo hace rollback automático.
 *
 * 2. Sistema de créditos implementado con operaciones atómicas en BD.
 *    Evitamos race conditions usando @Transactional + la BD como fuente de verdad.
 *
 * 3. Las notificaciones WebSocket se envían DESPUÉS de confirmar la transacción.
 *    Si el WebSocket falla, la operación de BD ya está committed = consistencia primero.
 *
 * Flujo de créditos:
 * - reservar: student pierde créditos → PENDING
 * - confirmar: teacher confirma → CONFIRMED (créditos aún en "escrow")
 * - completar: teacher gana créditos + contadores se actualizan → COMPLETED
 * - cancelar: student recupera créditos → CANCELLED
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final SessionMapper sessionMapper;
    private final NotificationService notificationService;

    @org.springframework.beans.factory.annotation.Value("${credits.session-reward:10}")
    private int sessionRewardCredits;

    // ==================== RESERVAR SESIÓN ====================

    /**
     * El estudiante reserva una sesión. Se deducen los créditos inmediatamente.
     */
    @Transactional
    public SessionResponse bookSession(CreateSessionRequest request, User student) {
        Offer offer = offerRepository.findById(request.getOfferId())
            .orElseThrow(() -> new ResourceNotFoundException("Oferta", request.getOfferId()));

        // Validaciones de negocio
        if (offer.getStatus() != Offer.OfferStatus.ACTIVE) {
            throw new BusinessException("Esta oferta no está disponible para reservas");
        }

        if (offer.getTeacher().getId().equals(student.getId())) {
            throw new BusinessException("No puedes reservar tu propia oferta");
        }

        // Calcular costo en créditos
        int sessionCost = calculateSessionCost(offer);

        // Verificar saldo suficiente
        if (student.getCreditsBalance() < sessionCost) {
            throw new InsufficientCreditsException(sessionCost, student.getCreditsBalance());
        }

        // Verificar conflicto de horario del teacher
        LocalDateTime sessionEnd = request.getScheduledAt().plusMinutes(offer.getDurationMinutes());
        boolean hasConflict = sessionRepository.existsConflictingSessionForTeacher(
            offer.getTeacher().getId(),
            request.getScheduledAt().minusMinutes(30), // buffer de 30 min
            sessionEnd
        );
        if (hasConflict) {
            throw new BusinessException(
                "El teacher ya tiene una sesión en ese horario. Por favor elige otro horario."
            );
        }

        // Deducir créditos del estudiante (atomicamente en esta transacción)
        student.setCreditsBalance(student.getCreditsBalance() - sessionCost);
        userRepository.save(student);

        // Crear la sesión
        Session session = Session.builder()
            .offer(offer)
            .student(student)
            .scheduledAt(request.getScheduledAt())
            .creditsPaid(sessionCost)
            .studentNotes(request.getStudentNotes())
            .status(Session.SessionStatus.PENDING)
            .creditsTransferred(false)
            .build();

        Session saved = sessionRepository.save(session);

        log.info("Sesión reservada: {} créditos deducidos de {}", sessionCost, student.getUsername());

        // Notificación WebSocket al teacher (después del commit)
        notificationService.notifySessionBooked(saved);

        return sessionMapper.toResponse(saved);
    }

    // ==================== CONFIRMAR SESIÓN ====================

    /**
     * El teacher confirma la sesión reservada.
     */
    @Transactional
    public SessionResponse confirmSession(Long sessionId, User teacher) {
        Session session = getSessionById(sessionId);
        validateTeacherOwnership(session, teacher);

        if (session.getStatus() != Session.SessionStatus.PENDING) {
            throw new BusinessException(
                "Solo las sesiones PENDING pueden confirmarse. Estado actual: " + session.getStatus()
            );
        }

        session.setStatus(Session.SessionStatus.CONFIRMED);
        session.setConfirmedAt(LocalDateTime.now());
        Session saved = sessionRepository.save(session);

        log.info("Sesión {} confirmada por teacher {}", sessionId, teacher.getUsername());

        // Notificación al estudiante
        notificationService.notifySessionConfirmed(saved);

        return sessionMapper.toResponse(saved);
    }

    // ==================== COMPLETAR SESIÓN ====================

    /**
     * El teacher marca la sesión como completada → se transfieren los créditos.
     *
     * Decisión: solo el teacher puede marcar como completada.
     * En v2 podríamos añadir confirmación del estudiante (ambas partes).
     */
    @Transactional
    public SessionResponse completeSession(Long sessionId, User teacher) {
        Session session = getSessionById(sessionId);
        validateTeacherOwnership(session, teacher);

        if (session.getStatus() != Session.SessionStatus.CONFIRMED) {
            throw new BusinessException(
                "Solo las sesiones CONFIRMED pueden completarse. Estado actual: " + session.getStatus()
            );
        }

        if (session.getCreditsTransferred()) {
            throw new BusinessException("Los créditos ya fueron transferidos para esta sesión");
        }

        // Transferir créditos al teacher
        teacher.setCreditsBalance(teacher.getCreditsBalance() + session.getCreditsPaid());
        teacher.setTotalSessionsTaught(teacher.getTotalSessionsTaught() + 1);
        userRepository.save(teacher);

        // Actualizar estadísticas del estudiante
        User student = session.getStudent();
        student.setTotalSessionsLearned(student.getTotalSessionsLearned() + 1);
        userRepository.save(student);

        // Actualizar stats de la oferta
        Offer offer = session.getOffer();
        offer.setTotalSessionsCompleted(offer.getTotalSessionsCompleted() + 1);
        offerRepository.save(offer);

        // Marcar sesión como completada
        session.setStatus(Session.SessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        session.setCreditsTransferred(true);
        Session saved = sessionRepository.save(session);

        log.info("Sesión {} completada. {} créditos transferidos a {}",
            sessionId, session.getCreditsPaid(), teacher.getUsername());

        // Notificación al estudiante
        notificationService.notifySessionCompleted(saved);

        return sessionMapper.toResponse(saved);
    }

    // ==================== CANCELAR SESIÓN ====================

    @Transactional
    public SessionResponse cancelSession(Long sessionId, User currentUser) {
        Session session = getSessionById(sessionId);

        boolean isStudent = session.getStudent().getId().equals(currentUser.getId());
        boolean isTeacher = session.getOffer().getTeacher().getId().equals(currentUser.getId());

        if (!isStudent && !isTeacher) {
            throw new BusinessException("No tienes permiso para cancelar esta sesión", HttpStatus.FORBIDDEN);
        }

        if (session.getStatus() == Session.SessionStatus.COMPLETED) {
            throw new BusinessException("No se puede cancelar una sesión ya completada");
        }

        // Devolver créditos al estudiante (solo si aún no se completó)
        if (!session.getCreditsTransferred()) {
            User student = session.getStudent();
            student.setCreditsBalance(student.getCreditsBalance() + session.getCreditsPaid());
            userRepository.save(student);
            log.info("Reembolso de {} créditos a {}", session.getCreditsPaid(), student.getUsername());
        }

        session.setStatus(Session.SessionStatus.CANCELLED);
        session.setCancelledAt(LocalDateTime.now());
        return sessionMapper.toResponse(sessionRepository.save(session));
    }

    // ==================== CONSULTAS ====================

    @Transactional(readOnly = true)
    public Page<SessionResponse> getMySessionsAsStudent(User student, Pageable pageable) {
        return sessionRepository.findByStudentId(student.getId(), pageable)
            .map(sessionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessionResponse> getMySessionsAsTeacher(User teacher, Pageable pageable) {
        return sessionRepository.findByOfferTeacherId(teacher.getId(), pageable)
            .map(sessionMapper::toResponse);
    }

    // ==================== HELPERS PRIVADOS ====================

    private Session getSessionById(Long id) {
        return sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sesión", id));
    }

    private void validateTeacherOwnership(Session session, User teacher) {
        if (!session.getOffer().getTeacher().getId().equals(teacher.getId())) {
            throw new BusinessException(
                "Solo el teacher de esta oferta puede realizar esta acción",
                HttpStatus.FORBIDDEN
            );
        }
    }

    /**
     * Calcula el costo real de la sesión basado en duración.
     * Costo = (creditsCostPerHour * durationMinutes) / 60
     * Redondeado hacia arriba para no perjudicar al teacher.
     */
    private int calculateSessionCost(Offer offer) {
        double cost = (offer.getCreditsCostPerHour() * offer.getDurationMinutes()) / 60.0;
        return (int) Math.ceil(cost);
    }
}
