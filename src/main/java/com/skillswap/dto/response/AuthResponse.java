package com.skillswap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Respuesta de autenticación con tokens JWT.
 *
 * Decisión: devolvemos tanto accessToken como refreshToken.
 * El accessToken es de corta duración (24h) y el refreshToken
 * de larga duración (7d) para mejorar la experiencia UX sin
 * comprometer la seguridad.
 */
@Data
@Builder
@Schema(description = "Respuesta de autenticación con tokens JWT")
public class AuthResponse {

    @Schema(description = "Token JWT de acceso (Bearer)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Tipo de token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Tiempo de expiración en milisegundos", example = "86400000")
    private Long expiresIn;

    @Schema(description = "Información básica del usuario autenticado")
    private UserSummaryResponse user;
}
