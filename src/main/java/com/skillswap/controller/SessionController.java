package com.skillswap.controller;

import com.skillswap.dto.request.CreateSessionRequest;
import com.skillswap.dto.response.ApiResponse;
import com.skillswap.dto.response.SessionResponse;
import com.skillswap.entity.User;
import com.skillswap.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Controller de sesiones — gestión del flujo de reservas.
 *
 * Todos los endpoints requieren autenticación JWT.
 *
 * Flujo PATCH:
 * - /confirm → teacher acepta la sesión
 * - /complete → teacher marca la sesión como terminada
 * - /cancel → cualquiera puede cancelar (con reembolso)
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Tag(name = "Sesiones", description = "Reservas y gestión del ciclo de vida de sesiones de aprendizaje")
@SecurityRequirement(name = "Bearer Authentication")
public class SessionController {

    private final SessionService sessionService;

    // ==================== RESERVAR SESIÓN ====================

    @PostMapping
    @Operation(summary = "Reservar una sesión", description = "El estudiante reserva una sesión. Se deducen créditos inmediatamente.")
    public ResponseEntity<ApiResponse<SessionResponse>> bookSession(
        @Valid @RequestBody CreateSessionRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        SessionResponse session = sessionService.bookSession(request, currentUser);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.created(session, "Sesion reservada exitosamente. Creditos deducidos."));
    }

    // ==================== CONFIRMAR SESIÓN ====================

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirmar sesión (teacher)", description = "El teacher acepta y confirma la sesión reservada")
    public ResponseEntity<ApiResponse<SessionResponse>> confirmSession(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            sessionService.confirmSession(id, currentUser),
            "Sesion confirmada!"
        ));
    }

    // ==================== COMPLETAR SESIÓN ====================

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Completar sesión (teacher)", description = "Marca la sesión como completada y transfiere los créditos al teacher")
    public ResponseEntity<ApiResponse<SessionResponse>> completeSession(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            sessionService.completeSession(id, currentUser),
            "Sesion completada! Creditos transferidos."
        ));
    }

    // ==================== CANCELAR SESIÓN ====================

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar sesión", description = "Cancela la sesión y reembolsa los créditos al estudiante")
    public ResponseEntity<ApiResponse<SessionResponse>> cancelSession(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            sessionService.cancelSession(id, currentUser),
            "Sesion cancelada. Creditos reembolsados."
        ));
    }

    // ==================== MIS SESIONES ====================

    @GetMapping("/my/student")
    @Operation(summary = "Mis sesiones como estudiante")
    public ResponseEntity<ApiResponse<Page<SessionResponse>>> getMySessionsAsStudent(
        @AuthenticationPrincipal User currentUser,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledAt"));
        return ResponseEntity.ok(ApiResponse.ok(
            sessionService.getMySessionsAsStudent(currentUser, pageable)
        ));
    }

    @GetMapping("/my/teacher")
    @Operation(summary = "Mis sesiones como teacher")
    public ResponseEntity<ApiResponse<Page<SessionResponse>>> getMySessionsAsTeacher(
        @AuthenticationPrincipal User currentUser,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledAt"));
        return ResponseEntity.ok(ApiResponse.ok(
            sessionService.getMySessionsAsTeacher(currentUser, pageable)
        ));
    }
}
