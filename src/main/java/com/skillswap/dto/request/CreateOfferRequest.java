package com.skillswap.dto.request;

import com.skillswap.entity.Offer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Datos para crear una nueva oferta de enseñanza")
public class CreateOfferRequest {

    @NotNull(message = "El ID de la habilidad es obligatorio")
    @Schema(description = "ID de la habilidad del catálogo", example = "1")
    private Long skillId;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 10, max = 150, message = "El título debe tener entre 10 y 150 caracteres")
    @Schema(description = "Título descriptivo de la oferta", example = "Aprende Python desde cero con proyectos reales")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 30, max = 2000, message = "La descripción debe tener entre 30 y 2000 caracteres")
    @Schema(description = "Descripción detallada de lo que enseñarás")
    private String description;

    @NotNull(message = "El costo en créditos es obligatorio")
    @Min(value = 1, message = "El mínimo es 1 crédito por hora")
    @Max(value = 100, message = "El máximo es 100 créditos por hora")
    @Schema(description = "Costo en créditos por hora", example = "5")
    private Integer creditsCostPerHour;

    @Min(value = 30, message = "La duración mínima es 30 minutos")
    @Max(value = 240, message = "La duración máxima es 4 horas (240 min)")
    @Schema(description = "Duración de la sesión en minutos", example = "60")
    private Integer durationMinutes = 60;

    @Schema(description = "Modalidad de la sesión", example = "ONLINE")
    private Offer.Modality modality = Offer.Modality.ONLINE;

    @Size(max = 255, message = "Los tags no pueden superar 255 caracteres")
    @Schema(description = "Tags separados por coma", example = "python,programacion,backend")
    private String tags;

    @Min(value = 1, message = "Mínimo 1 estudiante por sesión")
    @Max(value = 10, message = "Máximo 10 estudiantes por sesión")
    @Schema(description = "Máximo de estudiantes por sesión", example = "1")
    private Integer maxStudentsPerSession = 1;
}
