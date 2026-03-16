package com.skillswap.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / OpenAPI 3.
 *
 * Decisión arquitectónica: usar anotaciones en lugar de bean @Bean OpenAPI
 * para mantener la configuración declarativa y más legible.
 *
 * El SecurityScheme "Bearer Authentication" se referencia en cada
 * endpoint protegido con @SecurityRequirement(name = "Bearer Authentication").
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "SkillSwap API",
        version = "1.0.0",
        description = """
            ## SkillSwap — La plataforma donde el conocimiento es la moneda

            **SkillSwap** es una plataforma de intercambio de habilidades donde los usuarios
            enseñan lo que saben y aprenden lo que quieren, usando **creditos** en lugar de dinero.

            ### Como funciona:
            1. **Registro** → Recibes 50 creditos de bienvenida
            2. **Explora** → Busca ofertas de habilidades por categoría o texto libre
            3. **Reserva** → Reserva una sesion (se deducen creditos)
            4. **Aprende** → El teacher confirma y realizan la sesion
            5. **Valora** → Deja una review al completar

            ### Sistema de creditos:
            - Registrarse: +50 creditos
            - Completar una sesion como teacher: ganas los creditos del estudiante
            - Reservar como estudiante: gastas creditos segun duracion y precio/hora

            ### Autenticacion:
            Usa el endpoint `/auth/login` para obtener tu token JWT.
            Luego haz clic en **Authorize** e introduce: `Bearer <tu-token>`
            """,
        contact = @Contact(
            name = "SkillSwap Team",
            email = "dev@skillswap.app"
        ),
        license = @License(
            name = "MIT License"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080/api", description = "Servidor de desarrollo"),
        @Server(url = "https://api.skillswap.app", description = "Servidor de produccion")
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    in = SecuritySchemeIn.HEADER,
    description = "Introduce el token JWT obtenido del endpoint /auth/login"
)
public class SwaggerConfig {
    // La configuración se realiza completamente via anotaciones
    // No se necesitan @Bean adicionales con springdoc-openapi
}
