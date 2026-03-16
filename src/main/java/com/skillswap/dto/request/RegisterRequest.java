package com.skillswap.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO para registro de usuario.
 *
 * Decisión: usamos @Data de Lombok para DTOs (getter + setter + equals + hashCode + toString).
 * Para entidades usamos @Getter/@Setter separados para tener más control.
 */
@Data
@Schema(description = "Datos necesarios para registrar un nuevo usuario en SkillSwap")
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El username solo puede contener letras, números y guión bajo")
    @Schema(description = "Nombre de usuario único", example = "john_dev")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Schema(description = "Email del usuario", example = "john@example.com")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "La contraseña debe tener al menos una mayúscula, una minúscula y un número"
    )
    @Schema(description = "Contraseña (min 8 chars, 1 mayúscula, 1 número)", example = "SecurePass1")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre completo del usuario", example = "John Doe")
    private String fullName;

    @Size(max = 500, message = "La bio no puede superar 500 caracteres")
    @Schema(description = "Descripción personal breve", example = "Desarrollador apasionado por Python y música")
    private String bio;
}
