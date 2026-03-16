package com.skillswap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * SkillSwap — Plataforma de intercambio de habilidades.
 *
 * Decisión arquitectónica: @EnableJpaAuditing a nivel raíz para activar
 * automáticamente los campos createdAt/updatedAt en todas las entidades
 * que usen @EntityListeners(AuditingEntityListener.class).
 */
@SpringBootApplication
@EnableJpaAuditing
public class SkillSwapApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkillSwapApplication.class, args);
    }
}
