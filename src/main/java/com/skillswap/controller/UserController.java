package com.skillswap.controller;

import com.skillswap.dto.response.ApiResponse;
import com.skillswap.dto.response.UserProfileResponse;
import com.skillswap.dto.response.UserSummaryResponse;
import com.skillswap.entity.User;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.mapper.UserMapper;
import com.skillswap.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de perfil de usuario.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de perfiles de usuario")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @Operation(
        summary = "Obtener mi perfil completo",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
        @AuthenticationPrincipal User currentUser
    ) {
        // Recargar desde BD con JOIN FETCH de userSkills para evitar LazyInitializationException.
        // El objeto del SecurityContext no tiene sesión JPA activa.
        User freshUser = userRepository.findByIdWithSkills(currentUser.getId())
            .orElseThrow(() -> new com.skillswap.exception.ResourceNotFoundException("Usuario", currentUser.getId()));
        return ResponseEntity.ok(ApiResponse.ok(userMapper.toProfileResponse(freshUser)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver perfil público de un usuario")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return ResponseEntity.ok(ApiResponse.ok(userMapper.toSummaryResponse(user)));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Ver perfil por username")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getUserByUsername(
        @PathVariable String username
    ) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
        return ResponseEntity.ok(ApiResponse.ok(userMapper.toSummaryResponse(user)));
    }
}
