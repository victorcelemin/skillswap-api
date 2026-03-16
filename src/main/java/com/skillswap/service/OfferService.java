package com.skillswap.service;

import com.skillswap.dto.request.CreateOfferRequest;
import com.skillswap.dto.response.OfferResponse;
import com.skillswap.entity.Offer;
import com.skillswap.entity.Skill;
import com.skillswap.entity.User;
import com.skillswap.exception.BusinessException;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.mapper.OfferMapper;
import com.skillswap.repository.OfferRepository;
import com.skillswap.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de gestión de ofertas de enseñanza.
 *
 * Decisiones de arquitectura:
 * - @PreAuthorize para control de acceso a nivel de método (más granular que SecurityConfig)
 * - @Transactional en operaciones de escritura; @Transactional(readOnly=true) en lecturas
 * - Paginación en todos los listados para evitar problemas de memoria con datasets grandes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final SkillRepository skillRepository;
    private final OfferMapper offerMapper;

    // ==================== CREAR OFERTA ====================

    @Transactional
    public OfferResponse createOffer(CreateOfferRequest request, User currentUser) {
        Skill skill = skillRepository.findById(request.getSkillId())
            .orElseThrow(() -> new ResourceNotFoundException("Skill", request.getSkillId()));

        if (!skill.getIsActive()) {
            throw new BusinessException("La habilidad '" + skill.getName() + "' no está disponible actualmente");
        }

        Offer offer = Offer.builder()
            .teacher(currentUser)
            .skill(skill)
            .title(request.getTitle())
            .description(request.getDescription())
            .creditsCostPerHour(request.getCreditsCostPerHour())
            .durationMinutes(request.getDurationMinutes())
            .modality(request.getModality())
            .maxStudentsPerSession(request.getMaxStudentsPerSession())
            .tags(request.getTags())
            .status(Offer.OfferStatus.ACTIVE)
            .build();

        Offer saved = offerRepository.save(offer);

        // Actualizar contador de ofertas en la habilidad
        skill.setTotalOffers(skill.getTotalOffers() + 1);
        skillRepository.save(skill);

        log.info("Nueva oferta creada: '{}' por teacher {}", saved.getTitle(), currentUser.getUsername());
        return offerMapper.toResponse(saved);
    }

    // ==================== LISTAR OFERTAS ====================

    @Transactional(readOnly = true)
    public Page<OfferResponse> getAllActiveOffers(Pageable pageable) {
        return offerRepository.findByStatus(Offer.OfferStatus.ACTIVE, pageable)
            .map(offerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OfferResponse> searchOffers(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return getAllActiveOffers(pageable);
        }
        return offerRepository.searchByText(query.trim(), pageable)
            .map(offerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OfferResponse> getOffersByCategory(Skill.SkillCategory category, Pageable pageable) {
        return offerRepository.findByStatusAndSkill_Category(
            Offer.OfferStatus.ACTIVE, category, pageable
        ).map(offerMapper::toResponse);
    }

    // ==================== OBTENER OFERTA POR ID ====================

    @Transactional
    public OfferResponse getOfferById(Long id) {
        Offer offer = offerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Oferta", id));

        // Incrementar contador de vistas (no-transaccional en separado sería más óptimo)
        offer.setViewsCount(offer.getViewsCount() + 1);
        offerRepository.save(offer);

        return offerMapper.toResponse(offer);
    }

    // ==================== MIS OFERTAS ====================

    @Transactional(readOnly = true)
    public Page<OfferResponse> getMyOffers(User currentUser, Pageable pageable) {
        return offerRepository.findByTeacherId(currentUser.getId(), pageable)
            .map(offerMapper::toResponse);
    }

    // ==================== PAUSAR / CERRAR OFERTA ====================

    @Transactional
    public OfferResponse updateOfferStatus(Long offerId, Offer.OfferStatus newStatus, User currentUser) {
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException("Oferta", offerId));

        if (!offer.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException("Solo el teacher puede modificar esta oferta", HttpStatus.FORBIDDEN);
        }

        offer.setStatus(newStatus);
        return offerMapper.toResponse(offerRepository.save(offer));
    }
}
