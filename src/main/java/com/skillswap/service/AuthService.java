package com.skillswap.service;

import com.skillswap.dto.request.LoginRequest;
import com.skillswap.dto.request.RegisterRequest;
import com.skillswap.dto.response.AuthResponse;
import com.skillswap.dto.response.UserSummaryResponse;
import com.skillswap.entity.User;
import com.skillswap.exception.BusinessException;
import com.skillswap.mapper.UserMapper;
import com.skillswap.repository.UserRepository;
import com.skillswap.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación.
 *
 * Decisión arquitectónica: @Transactional en register para garantizar
 * que si algo falla después de crear el usuario (ej: enviar email de
 * bienvenida futuro), el usuario no quede a medias en la BD.
 *
 * Separamos register del AuthenticationManager de Spring porque:
 * 1. register no necesita authentication
 * 2. Nos da más control sobre la lógica de creación
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Value("${credits.initial-balance:50}")
    private int initialCreditsBalance;

    // ==================== REGISTRO ====================

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validaciones de unicidad
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email '" + request.getEmail() + "' ya está registrado");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El username '" + request.getUsername() + "' ya está en uso");
        }

        // Construir usuario con créditos iniciales configurables
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .bio(request.getBio())
            .creditsBalance(initialCreditsBalance)
            .role(User.Role.USER)
            .isActive(true)
            .build();

        User savedUser = userRepository.save(user);
        log.info("Nuevo usuario registrado: {} ({})", savedUser.getUsername(), savedUser.getEmail());

        String jwtToken = jwtService.generateToken(savedUser);
        UserSummaryResponse userSummary = userMapper.toSummaryResponse(savedUser);

        return AuthResponse.builder()
            .accessToken(jwtToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getJwtExpiration())
            .user(userSummary)
            .build();
    }

    // ==================== LOGIN ====================

    public AuthResponse login(LoginRequest request) {
        // Buscar usuario por email o username
        User user = userRepository.findByEmailOrUsername(request.getIdentifier())
            .orElseThrow(() -> new BusinessException(
                "No existe cuenta con ese email o username",
                org.springframework.http.HttpStatus.UNAUTHORIZED
            ));

        // Delegar autenticación al AuthenticationManager de Spring
        // Esto valida la contraseña con BCrypt automáticamente
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                user.getUsername(),  // Spring Security usa username como principal
                request.getPassword()
            )
        );

        String jwtToken = jwtService.generateToken(user);
        UserSummaryResponse userSummary = userMapper.toSummaryResponse(user);

        log.info("Login exitoso: {}", user.getUsername());

        return AuthResponse.builder()
            .accessToken(jwtToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getJwtExpiration())
            .user(userSummary)
            .build();
    }
}
