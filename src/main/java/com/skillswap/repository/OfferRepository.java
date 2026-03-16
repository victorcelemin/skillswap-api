package com.skillswap.repository;

import com.skillswap.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de ofertas.
 *
 * Decisión: extendemos JpaSpecificationExecutor para poder construir
 * queries dinámicas con filtros (categoría, precio, rating) sin
 * proliferación de métodos findBy...And...Or...
 *
 * El método de búsqueda con texto libre usa ILIKE (case-insensitive LIKE de PostgreSQL)
 * a través de LOWER + LIKE portable con JPA.
 */
@Repository
public interface OfferRepository extends JpaRepository<Offer, Long>, JpaSpecificationExecutor<Offer> {

    Page<Offer> findByStatusAndSkill_Category(
        Offer.OfferStatus status,
        com.skillswap.entity.Skill.SkillCategory category,
        Pageable pageable
    );

    Page<Offer> findByStatus(Offer.OfferStatus status, Pageable pageable);

    Page<Offer> findByTeacherId(Long teacherId, Pageable pageable);

    /**
     * Búsqueda de texto libre en título y descripción.
     * Usa LOWER para case-insensitive sin depender de índices específicos de BD.
     */
    @Query("""
        SELECT o FROM Offer o
        WHERE o.status = 'ACTIVE'
        AND (
            LOWER(o.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(o.description) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(o.tags) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        """)
    Page<Offer> searchByText(@Param("query") String query, Pageable pageable);

    @Query("""
        SELECT o FROM Offer o
        WHERE o.status = 'ACTIVE'
        AND o.creditsCostPerHour BETWEEN :minCredits AND :maxCredits
        """)
    Page<Offer> findByCreditRange(
        @Param("minCredits") int minCredits,
        @Param("maxCredits") int maxCredits,
        Pageable pageable
    );
}
