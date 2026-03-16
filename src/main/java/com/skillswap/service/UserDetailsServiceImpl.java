package com.skillswap.service;

import com.skillswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de UserDetailsService para Spring Security.
 *
 * Decisión: separamos esta implementación del AuthService para
 * respetar el Single Responsibility Principle. AuthService maneja
 * registro/login mientras esta clase maneja la carga de usuarios
 * para el contexto de seguridad (usado internamente por Spring).
 *
 * @Transactional(readOnly = true) optimiza la performance de lectura
 * (sin overhead de transacciones de escritura).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() ->
                new UsernameNotFoundException("Usuario no encontrado: " + username)
            );
    }
}
