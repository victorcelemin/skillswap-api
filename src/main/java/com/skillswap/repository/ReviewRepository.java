package com.skillswap.repository;

import com.skillswap.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    Page<Review> findByTeacherIdAndIsPublicTrue(Long teacherId, Pageable pageable);

    Page<Review> findByReviewerId(Long reviewerId, Pageable pageable);

    /**
     * Calcula el rating promedio de un teacher para actualizar su perfil.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.teacher.id = :teacherId AND r.isPublic = true")
    Optional<Double> calculateAverageRatingByTeacherId(@Param("teacherId") Long teacherId);
}
