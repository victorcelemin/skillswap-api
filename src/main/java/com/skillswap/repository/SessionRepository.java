package com.skillswap.repository;

import com.skillswap.entity.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Page<Session> findByStudentId(Long studentId, Pageable pageable);

    Page<Session> findByOfferTeacherId(Long teacherId, Pageable pageable);

    Page<Session> findByStudentIdAndStatus(Long studentId, Session.SessionStatus status, Pageable pageable);

    Page<Session> findByOfferTeacherIdAndStatus(Long teacherId, Session.SessionStatus status, Pageable pageable);

    /**
     * Verifica si ya existe una sesión activa solapada para el teacher.
     * Evita que un teacher tenga dos sesiones al mismo tiempo.
     */
    @Query("""
        SELECT COUNT(s) > 0 FROM Session s
        WHERE s.offer.teacher.id = :teacherId
        AND s.status IN ('PENDING', 'CONFIRMED')
        AND s.scheduledAt BETWEEN :start AND :end
        """)
    boolean existsConflictingSessionForTeacher(
        @Param("teacherId") Long teacherId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Obtiene sesiones pendientes de confirmación por teacher.
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.offer.teacher.id = :teacherId
        AND s.status = 'PENDING'
        ORDER BY s.scheduledAt ASC
        """)
    List<Session> findPendingSessionsForTeacher(@Param("teacherId") Long teacherId);
}
