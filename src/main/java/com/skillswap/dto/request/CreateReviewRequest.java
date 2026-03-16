package com.skillswap.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Datos para crear una review de sesión completada")
public class CreateReviewRequest {

    @NotNull(message = "El ID de la sesión es obligatorio")
    @Schema(description = "ID de la sesión completada", example = "1")
    private Long sessionId;

    @NotNull(message = "El rating es obligatorio")
    @Min(value = 1, message = "El rating mínimo es 1")
    @Max(value = 5, message = "El rating máximo es 5")
    @Schema(description = "Valoración de 1 a 5 estrellas", example = "5")
    private Integer rating;

    @Size(max = 1000, message = "El comentario no puede superar 1000 caracteres")
    @Schema(description = "Comentario sobre la experiencia", example = "Excelente clase, muy claro y paciente")
    private String comment;

    @Schema(description = "¿La review es pública?", example = "true")
    private Boolean isPublic = true;
}
