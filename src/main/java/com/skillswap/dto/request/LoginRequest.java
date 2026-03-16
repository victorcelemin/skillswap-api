package com.skillswap.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credenciales de login")
public class LoginRequest {

    @NotBlank(message = "El email o username es obligatorio")
    @Schema(description = "Email o username del usuario", example = "john@example.com")
    private String identifier;  // Acepta email O username

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña", example = "SecurePass1")
    private String password;
}
