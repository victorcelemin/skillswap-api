package com.skillswap.controller;

import com.skillswap.dto.request.CreateOfferRequest;
import com.skillswap.dto.response.ApiResponse;
import com.skillswap.dto.response.OfferResponse;
import com.skillswap.entity.Offer;
import com.skillswap.entity.Skill;
import com.skillswap.entity.User;
import com.skillswap.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de ofertas de enseñanza.
 *
 * Decisión: @AuthenticationPrincipal inyecta el objeto User directamente
 * desde el SecurityContext, evitando pasar el JWT/username manualmente.
 * Esto funciona porque User implementa UserDetails.
 *
 * Paginación:
 * - page: número de página (0-indexed)
 * - size: elementos por página (default 12 — pensado en grids de 3x4)
 * - sort: campo,dirección (ej: createdAt,desc)
 */
@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
@Tag(name = "Ofertas", description = "Gestión de ofertas de enseñanza de habilidades")
public class OfferController {

    private final OfferService offerService;

    // ==================== CREAR OFERTA ====================

    @PostMapping
    @Operation(
        summary = "Crear nueva oferta",
        description = "El usuario autenticado crea una oferta para enseñar una habilidad",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<ApiResponse<OfferResponse>> createOffer(
        @Valid @RequestBody CreateOfferRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        OfferResponse offer = offerService.createOffer(request, currentUser);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.created(offer, "Oferta creada exitosamente"));
    }

    // ==================== LISTAR OFERTAS (PÚBLICO) ====================

    @GetMapping
    @Operation(summary = "Listar todas las ofertas activas", description = "Endpoint público con paginación y filtros")
    public ResponseEntity<ApiResponse<Page<OfferResponse>>> getAllOffers(
        @Parameter(description = "Término de búsqueda libre")
        @RequestParam(required = false) String q,

        @Parameter(description = "Filtrar por categoría")
        @RequestParam(required = false) Skill.SkillCategory category,

        @Parameter(description = "Número de página (0-indexed)")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Elementos por página")
        @RequestParam(defaultValue = "12") int size,

        @Parameter(description = "Campo de ordenación")
        @RequestParam(defaultValue = "createdAt") String sortBy,

        @Parameter(description = "Dirección de ordenación")
        @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OfferResponse> offers;
        if (q != null && !q.isBlank()) {
            offers = offerService.searchOffers(q, pageable);
        } else if (category != null) {
            offers = offerService.getOffersByCategory(category, pageable);
        } else {
            offers = offerService.getAllActiveOffers(pageable);
        }

        return ResponseEntity.ok(ApiResponse.ok(offers));
    }

    // ==================== OBTENER OFERTA POR ID (PÚBLICO) ====================

    @GetMapping("/{id}")
    @Operation(summary = "Obtener oferta por ID", description = "Incrementa el contador de vistas")
    public ResponseEntity<ApiResponse<OfferResponse>> getOfferById(
        @Parameter(description = "ID de la oferta")
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(offerService.getOfferById(id)));
    }

    // ==================== MIS OFERTAS ====================

    @GetMapping("/my")
    @Operation(
        summary = "Mis ofertas como teacher",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<ApiResponse<Page<OfferResponse>>> getMyOffers(
        @AuthenticationPrincipal User currentUser,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.ok(offerService.getMyOffers(currentUser, pageable)));
    }

    // ==================== CAMBIAR ESTADO ====================

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Cambiar estado de una oferta",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<ApiResponse<OfferResponse>> updateOfferStatus(
        @PathVariable Long id,
        @RequestParam Offer.OfferStatus status,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            offerService.updateOfferStatus(id, status, currentUser),
            "Estado de oferta actualizado"
        ));
    }
}
