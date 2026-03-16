package com.skillswap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración WebSocket con STOMP.
 *
 * Decisiones arquitectónicas:
 * 1. Broker simple en memoria (SimpleBroker) — suficiente para una instancia.
 *    En producción con múltiples instancias, usar ActiveMQ/RabbitMQ con STOMP relay.
 *
 * 2. Prefijos separados:
 *    - /app: rutas de controladores WebSocket (@MessageMapping)
 *    - /topic: suscripciones públicas (broadcast)
 *    - /user: suscripciones privadas por usuario (queue)
 *
 * 3. SockJS como fallback: permite funcionar en entornos sin soporte WebSocket nativo.
 *    Esto es especialmente importante para clientes detrás de proxies corporativos.
 *
 * Arquitectura de notificaciones:
 * - Eventos de sesión → /user/{username}/queue/notifications (privado por usuario)
 * - Stats globales → /topic/stats (broadcast a todos)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Broker en memoria para /topic (broadcast) y /user (privado)
        config.enableSimpleBroker("/topic", "/user");

        // Prefijo para @MessageMapping — los clientes envían a /app/...
        config.setApplicationDestinationPrefixes("/app");

        // Prefijo para mensajes privados de usuario
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            .addEndpoint("/ws")  // URL de conexión WebSocket
            .setAllowedOriginPatterns("*")  // En prod: dominios específicos
            .withSockJS();  // Fallback para browsers sin WebSocket nativo
    }
}
