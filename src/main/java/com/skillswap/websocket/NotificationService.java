package com.skillswap.websocket;

import com.skillswap.entity.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio de notificaciones en tiempo real via WebSocket (STOMP).
 *
 * Decisión arquitectónica: usamos SimpMessagingTemplate para enviar
 * mensajes tanto a usuarios específicos (/user/...) como a topics
 * públicos (/topic/...).
 *
 * Por qué separar en un @Service:
 * - Los controllers de negocio no saben nada de WebSocket
 * - Fácil de mockear en tests
 * - Puede evolucionar a un event-driven system (ApplicationEvents)
 *
 * Destinos de notificación:
 * - /user/{username}/queue/notifications — notificaciones privadas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // ==================== EVENTOS DE SESIÓN ====================

    /**
     * Notifica al teacher cuando un estudiante reserva una sesión.
     */
    public void notifySessionBooked(Session session) {
        String teacherUsername = session.getOffer().getTeacher().getUsername();
        String studentUsername = session.getStudent().getUsername();

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationEvent.EventType.SESSION_BOOKED)
            .title("Nueva sesion reservada!")
            .message(studentUsername + " ha reservado tu sesion para " + session.getScheduledAt())
            .data(buildSessionSummary(session))
            .timestamp(java.time.LocalDateTime.now())
            .build();

        sendToUser(teacherUsername, event);
        log.debug("Notificacion SESSION_BOOKED enviada a {}", teacherUsername);
    }

    /**
     * Notifica al estudiante cuando el teacher confirma la sesión.
     */
    public void notifySessionConfirmed(Session session) {
        String studentUsername = session.getStudent().getUsername();
        String teacherUsername = session.getOffer().getTeacher().getUsername();

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationEvent.EventType.SESSION_CONFIRMED)
            .title("Sesion confirmada!")
            .message(teacherUsername + " ha confirmado tu sesion")
            .data(buildSessionSummary(session))
            .timestamp(java.time.LocalDateTime.now())
            .build();

        sendToUser(studentUsername, event);
        log.debug("Notificacion SESSION_CONFIRMED enviada a {}", studentUsername);
    }

    /**
     * Notifica al estudiante y teacher cuando se completa una sesión.
     */
    public void notifySessionCompleted(Session session) {
        String studentUsername = session.getStudent().getUsername();
        String teacherUsername = session.getOffer().getTeacher().getUsername();
        int creditsEarned = session.getCreditsPaid();

        // Notificar al teacher: créditos recibidos
        NotificationEvent teacherEvent = NotificationEvent.builder()
            .type(NotificationEvent.EventType.SESSION_COMPLETED)
            .title("Sesion completada!")
            .message("+" + creditsEarned + " creditos acreditados en tu cuenta!")
            .data(buildSessionSummary(session))
            .timestamp(java.time.LocalDateTime.now())
            .build();

        // Notificar al estudiante: puede dejar su review
        NotificationEvent studentEvent = NotificationEvent.builder()
            .type(NotificationEvent.EventType.SESSION_COMPLETED)
            .title("Sesion completada!")
            .message("Tu sesion finalizó. Deja una review para " + teacherUsername)
            .data(buildSessionSummary(session))
            .timestamp(java.time.LocalDateTime.now())
            .build();

        sendToUser(teacherUsername, teacherEvent);
        sendToUser(studentUsername, studentEvent);
        log.debug("Notificaciones SESSION_COMPLETED enviadas a {} y {}", teacherUsername, studentUsername);
    }

    // ==================== HELPERS PRIVADOS ====================

    private void sendToUser(String username, NotificationEvent event) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            event
        );
    }

    /**
     * Crea un resumen de la sesión para incluir en la notificación.
     * Evitamos serializar toda la entidad para reducir el payload WebSocket.
     */
    private java.util.Map<String, Object> buildSessionSummary(Session session) {
        return java.util.Map.of(
            "sessionId", session.getId(),
            "offerId", session.getOffer().getId(),
            "offerTitle", session.getOffer().getTitle(),
            "scheduledAt", session.getScheduledAt().toString(),
            "creditsPaid", session.getCreditsPaid(),
            "status", session.getStatus().name()
        );
    }
}
