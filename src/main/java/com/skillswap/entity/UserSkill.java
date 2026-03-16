package com.skillswap.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad UserSkill — tabla intermedia User <-> Skill con metadatos.
 *
 * Decisión arquitectónica: en lugar de una tabla de join simple (@ManyToMany),
 * usamos una entidad intermedia explícita porque necesitamos metadatos
 * adicionales: nivel de habilidad, tipo (enseña/aprende), años de experiencia.
 * Esto es el patrón "Association Table with Attributes".
 */
@Entity
@Table(name = "user_skills",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_skill_type",
            columnNames = {"user_id", "skill_id", "type"}
        )
    },
    indexes = {
        @Index(name = "idx_user_skill_user", columnList = "user_id"),
        @Index(name = "idx_user_skill_skill", columnList = "skill_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    /**
     * TEACHING: el usuario puede enseñar esta habilidad.
     * LEARNING: el usuario quiere aprender esta habilidad.
     * Un usuario puede tener la misma habilidad en ambos tipos (quiero aprender Y enseñar).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SkillType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    @Builder.Default
    private SkillLevel level = SkillLevel.BEGINNER;

    @Column(name = "years_experience")
    @Builder.Default
    private Integer yearsExperience = 0;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ==================== ENUMS ====================

    public enum SkillType {
        TEACHING, LEARNING
    }

    public enum SkillLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }
}
