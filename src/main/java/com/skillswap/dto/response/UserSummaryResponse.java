package com.skillswap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Versión reducida del usuario para uso en responses de otras entidades.
 * Evita enviar datos sensibles (password, etc.) y reduce el payload.
 */
@Data
@Builder
@Schema(description = "Resumen de información del usuario")
public class UserSummaryResponse {

    @Schema(description = "ID del usuario")
    private Long id;

    @Schema(description = "Username", example = "john_dev")
    private String username;

    @Schema(description = "Nombre completo", example = "John Doe")
    private String fullName;

    @Schema(description = "URL del avatar")
    private String avatarUrl;

    @Schema(description = "Saldo de créditos", example = "50")
    private Integer creditsBalance;

    @Schema(description = "Rating promedio como teacher", example = "4.8")
    private Double averageRating;

    @Schema(description = "Total de sesiones enseñadas", example = "15")
    private Integer totalSessionsTaught;
}
