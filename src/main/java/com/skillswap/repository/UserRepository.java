package com.skillswap.repository;

import com.skillswap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de usuarios.
 *
 * Decisión: extendemos JpaRepository (no CrudRepository) para tener
 * acceso a paginación y sorting out-of-the-box.
 *
 * findByEmailOrUsername: permite login con email O username en una sola query.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    /**
     * Busca por email o username — usado en el login con identifier genérico.
     * Decisión: una sola query en lugar de dos llamadas separadas = mejor performance.
     */
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier")
    Optional<User> findByEmailOrUsername(String identifier);

    /**
     * Carga el usuario con sus habilidades en una sola query (JOIN FETCH).
     * Evita LazyInitializationException al mapear el perfil completo.
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userSkills us LEFT JOIN FETCH us.skill WHERE u.id = :id")
    Optional<User> findByIdWithSkills(Long id);
}
