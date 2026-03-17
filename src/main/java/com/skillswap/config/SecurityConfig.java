package com.skillswap.config;

import com.skillswap.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuración de Spring Security.
 *
 * CORS:
 *   Los orígenes permitidos se leen desde la variable de entorno CORS_ALLOWED_ORIGINS
 *   (lista separada por comas). Esto evita recompilar al añadir nuevos dominios de frontend.
 *
 *   Ref oficial Spring: https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html
 *
 *   Regla crítica: con allowCredentials=true NO se puede usar el patrón "*".
 *   Se usa setAllowedOriginPatterns() que sí admite wildcards con credentials.
 *
 * PREFLIGHT:
 *   Spring Security intercepta las peticiones OPTIONS antes de que lleguen al
 *   CorsFilter si no hay un .cors() configurado correctamente. El orden correcto es:
 *   1. .csrf(disable)
 *   2. .cors(cors -> cors.configurationSource(...))
 *   3. .authorizeHttpRequests(...)
 *   Las rutas OPTIONS deben ser permitidas implícitamente via CorsConfiguration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Orígenes CORS permitidos. Se leen desde la variable de entorno CORS_ALLOWED_ORIGINS.
     * Si no está definida, usa los valores por defecto de development.
     *
     * Ejemplo de valor en Railway:
     *   CORS_ALLOWED_ORIGINS=https://skillswap-frontend-rouge.vercel.app,https://skillswap.vercel.app
     */
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:4200}")
    private String corsAllowedOrigins;

    // ==================== SECURITY FILTER CHAIN ====================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF deshabilitado: usamos JWT stateless, no cookies de sesión
            .csrf(AbstractHttpConfigurer::disable)

            // CORS configurado ANTES de las reglas de autorización.
            // Spring Security aplica CORS primero; si el preflight OPTIONS pasa CORS,
            // no llega al filtro de autenticación.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .authorizeHttpRequests(auth -> auth
                // ---- Públicos ----
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()   // todos los preflights
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/offers/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/skills/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/users/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/reviews/teacher/**").permitAll()
                // ---- WebSocket handshake ----
                .requestMatchers("/ws/**").permitAll()
                // ---- Todo lo demás requiere autenticación ----
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ==================== AUTENTICACIÓN ====================

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ==================== CORS ====================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Parsear la lista de orígenes desde la variable de entorno
        List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

        /**
         * setAllowedOriginPatterns() soporta wildcards ("https://*.vercel.app")
         * Y es compatible con allowCredentials=true, a diferencia de
         * setAllowedOrigins("*") que lanzaría IllegalArgumentException.
         *
         * Ref: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/cors/CorsConfiguration.html#setAllowedOriginPatterns(java.util.List)
         */
        config.setAllowedOriginPatterns(origins);

        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        config.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // Exponer Authorization en responses para que el cliente pueda leerlo
        config.setExposedHeaders(List.of("Authorization"));

        // Permitir envío de cookies/credenciales (necesario para algunos browsers)
        config.setAllowCredentials(true);

        // Cache del preflight: 1 hora
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
