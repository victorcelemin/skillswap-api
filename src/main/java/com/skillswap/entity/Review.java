package com.skillswap.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad Review — evaluación post-sesión del estudiante al teacher.
 *
 * Decisión arquitectónica: la review está vinculada a una Session específica
 * (no directamente a un User o Offer). Esto garantiza:
 * 1. Solo se puede reseñar una sesión completada.
 * 2. Solo el estudiante de esa sesión puede dejar la review.
 * 3. Una sesión = máximo una review (OneToOne).
 *
 * El rating es de 1-5 estrellas (representado como Integer para simplicidad).
 */
@Entity
@Table(name = "reviews",
    indexes = {
        @Index(name = "idx_review_session", columnList = "session_id"),
        @Index(name = "idx_review_reviewer", columnList = "reviewer_id"),
        @Index(name = "idx_review_rating", columnList = "rating")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    /**
     * Relación 1:1 con Session.
     * La FK review.session_id garantiza unicidad: solo una review por sesión.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private Session session;

    /**
     * El estudiante que escribe la review.
     * Denormalizado aquí para queries directas sin pasar por Session.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    /**
     * El teacher que recibe la review.
     * Denormalizado para queries de "reviews de un teacher".
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    /**
     * Rating: 1-5 estrellas.
     * Usamos @Column con check constraint en el comentario — en producción
     * añadir a nivel DB: CHECK (rating >= 1 AND rating <= 5)
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;
}
