package com.skillswap.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entidad User — implementa UserDetails de Spring Security directamente.
 *
 * Decisión arquitectónica: implementar UserDetails en la entidad elimina
 * la necesidad de un wrapper/adapter adicional. Es un patrón pragmático
 * válido para proyectos medianos donde no hay complejidad de múltiples
 * fuentes de autenticación.
 *
 * El sistema de créditos usa Integer (no BigDecimal) porque los créditos
 * son enteros, no fracciones monetarias.
 */
@Entity
@Table(name = "users",
    indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_username", columnList = "username", unique = true)
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    /**
     * Créditos disponibles. Inicial: 50 (configurado en application.yml).
     * Decisión: usar @Builder.Default para que el builder de Lombok respete el valor inicial.
     */
    @Builder.Default
    @Column(name = "credits_balance", nullable = false)
    private Integer creditsBalance = 50;

    @Column(name = "total_sessions_taught")
    @Builder.Default
    private Integer totalSessionsTaught = 0;

    @Column(name = "total_sessions_learned")
    @Builder.Default
    private Integer totalSessionsLearned = 0;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ==================== RELACIONES ====================

    /**
     * Relación 1:N con UserSkill.
     * CascadeType.ALL: si borramos el usuario, borramos sus habilidades.
     * orphanRemoval: limpia registros huérfanos automáticamente.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserSkill> userSkills;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Offer> offers;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<Session> sessionsAsStudent;

    // ==================== UserDetails Implementation ====================
    // Decisión: override explícito necesario porque Lombok genera getUsername()
    // pero UserDetails requiere que el método sea parte de la implementación
    // de la interfaz. Sin @Override explícito, el compilador puede confundirse.

    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }

    // ==================== ENUM ====================

    public enum Role {
        USER, ADMIN
    }
}
