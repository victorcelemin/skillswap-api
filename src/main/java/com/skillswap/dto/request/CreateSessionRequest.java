package com.skillswap.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Datos para reservar una sesión")
public class CreateSessionRequest {

    @NotNull(message = "El ID de la oferta es obligatorio")
    @Schema(description = "ID de la oferta a reservar", example = "1")
    private Long offerId;

    @NotNull(message = "La fecha y hora es obligatoria")
    @Future(message = "La sesión debe ser en el futuro")
    @Schema(description = "Fecha y hora de la sesión", example = "2024-12-25T15:00:00")
    private LocalDateTime scheduledAt;

    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    @Schema(description = "Notas o preguntas previas para el teacher")
    private String studentNotes;
}
