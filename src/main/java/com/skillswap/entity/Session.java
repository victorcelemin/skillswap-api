package com.skillswap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Session — reserva de una sesión de aprendizaje.
 *
 * Decisión arquitectónica: la Session es el núcleo transaccional
 * de la plataforma. Es donde ocurre la transferencia de créditos.
 *
 * Flujo de estados:
 * PENDING -> CONFIRMED -> COMPLETED
 *         -> REJECTED (teacher rechaza)
 *         -> CANCELLED (cualquiera cancela)
 *         -> NO_SHOW (nadie apareció)
 *
 * Los créditos se reservan (deducen del estudiante) al crear PENDING.
 * Se liberan al teacher solo al pasar a COMPLETED.
 * Se devuelven al estudiante si CANCELLED/REJECTED.
 */
@Entity
@Table(name = "sessions",
    indexes = {
        @Index(name = "idx_session_offer", columnList = "offer_id"),
        @Index(name = "idx_session_student", columnList = "student_id"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_scheduled", columnList = "scheduled_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * Créditos que el estudiante pagó (snapshot del costo en el momento de reserva).
     * Importante: guardamos el snapshot para que cambios futuros en la oferta
     * no afecten sesiones ya reservadas.
     */
    @Column(name = "credits_paid", nullable = false)
    private Integer creditsPaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.PENDING;

    @Column(name = "student_notes", columnDefinition = "TEXT")
    private String studentNotes;

    @Column(name = "teacher_notes", columnDefinition = "TEXT")
    private String teacherNotes;

    @Column(name = "meeting_url")
    private String meetingUrl;

    /**
     * Flag para saber si el teacher ya recibió sus créditos.
     * Evita doble acreditación en caso de reintentos.
     */
    @Column(name = "credits_transferred", nullable = false)
    @Builder.Default
    private Boolean creditsTransferred = false;

    // ==================== RELACIONES ====================

    /**
     * Una sesión puede tener exactamente una review (o ninguna).
     * mappedBy en Review para que la FK esté en la tabla reviews.
     */
    @OneToOne(mappedBy = "session", fetch = FetchType.LAZY)
    private Review review;

    // ==================== ENUM ====================

    public enum SessionStatus {
        PENDING,
        CONFIRMED,
        COMPLETED,
        CANCELLED,
        REJECTED,
        NO_SHOW
    }
}
