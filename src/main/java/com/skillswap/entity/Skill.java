package com.skillswap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entidad Skill — catálogo global de habilidades.
 *
 * Decisión arquitectónica: las habilidades son un catálogo centralizado
 * (no por usuario). Esto permite normalizar y buscar/filtrar offers
 * por habilidad de forma eficiente. Un usuario "tiene" habilidades
 * a través de UserSkill (tabla intermedia con metadatos).
 */
@Entity
@Table(name = "skills",
    indexes = {
        @Index(name = "idx_skill_name", columnList = "name", unique = true),
        @Index(name = "idx_skill_category", columnList = "category")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Categoría para agrupar habilidades (Tecnología, Música, Idiomas, Arte, etc.)
     * Usamos Enum para garantizar consistencia y permitir filtros eficientes.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private SkillCategory category;

    @Column(name = "icon_url")
    private String iconUrl;

    /**
     * Color hex para la UI — hace que cada skill tenga identidad visual única.
     * Ej: #6C63FF para programación, #FF6584 para música.
     */
    @Column(name = "color_hex", length = 7)
    @Builder.Default
    private String colorHex = "#6C63FF";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "total_offers")
    @Builder.Default
    private Integer totalOffers = 0;

    // ==================== RELACIONES ====================

    @OneToMany(mappedBy = "skill", fetch = FetchType.LAZY)
    private List<UserSkill> userSkills;

    @OneToMany(mappedBy = "skill", fetch = FetchType.LAZY)
    private List<Offer> offers;

    // ==================== ENUM ====================

    public enum SkillCategory {
        TECHNOLOGY,
        LANGUAGES,
        MUSIC,
        ART_AND_DESIGN,
        BUSINESS,
        SCIENCE,
        SPORTS_AND_FITNESS,
        COOKING,
        PERSONAL_DEVELOPMENT,
        OTHER
    }
}
