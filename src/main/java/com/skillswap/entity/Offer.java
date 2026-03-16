package com.skillswap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entidad Offer — oferta de enseñanza publicada por un teacher.
 *
 * Decisión arquitectónica: separar Offer de UserSkill permite que
 * un teacher publique múltiples ofertas con diferentes condiciones
 * para la misma habilidad (ej: "Python básico - 5 créditos/hora",
 * "Python avanzado - 10 créditos/hora").
 *
 * El campo creditsCostPerHour usa Integer porque los créditos son enteros.
 */
@Entity
@Table(name = "offers",
    indexes = {
        @Index(name = "idx_offer_teacher", columnList = "teacher_id"),
        @Index(name = "idx_offer_skill", columnList = "skill_id"),
        @Index(name = "idx_offer_status", columnList = "status"),
        @Index(name = "idx_offer_credits", columnList = "credits_cost_per_hour")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer extends BaseEntity {

    /**
     * El teacher que publica la oferta.
     * LAZY fetch: no necesitamos el usuario completo en cada query de offers.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Costo en créditos por hora de sesión.
     * Rango sugerido: 1-50 créditos/hora.
     */
    @Column(name = "credits_cost_per_hour", nullable = false)
    private Integer creditsCostPerHour;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 60;

    /**
     * Modalidad: online (Zoom/Meet) o presencial.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "modality", nullable = false)
    @Builder.Default
    private Modality modality = Modality.ONLINE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OfferStatus status = OfferStatus.ACTIVE;

    @Column(name = "max_students_per_session")
    @Builder.Default
    private Integer maxStudentsPerSession = 1;

    @Column(name = "tags", length = 255)
    private String tags;  // CSV: "python,backend,api"

    @Column(name = "total_sessions_completed")
    @Builder.Default
    private Integer totalSessionsCompleted = 0;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "views_count")
    @Builder.Default
    private Integer viewsCount = 0;

    // ==================== RELACIONES ====================

    @OneToMany(mappedBy = "offer", fetch = FetchType.LAZY)
    private List<Session> sessions;

    // ==================== ENUMS ====================

    public enum OfferStatus {
        ACTIVE, PAUSED, CLOSED
    }

    public enum Modality {
        ONLINE, IN_PERSON, HYBRID
    }
}
