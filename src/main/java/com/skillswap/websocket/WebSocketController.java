package com.skillswap.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller WebSocket — maneja mensajes STOMP entrantes del cliente.
 *
 * Decisión: el prefijo /app es configurado en WebSocketConfig.
 * Un cliente STOMP enviaría a /app/ping y recibiría en /user/queue/pong.
 *
 * Casos de uso:
 * - Ping/heartbeat para mantener conexión activa
 * - Suscripción a notificaciones al conectarse
 *
 * La mayoría de notificaciones se envían DESDE el servidor (push),
 * no en respuesta a mensajes del cliente. Por eso este controller
 * es mínimo — las notificaciones principales están en NotificationService.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    /**
     * Heartbeat — el cliente puede enviar pings para mantener la conexión.
     * Responde al usuario que envió el mensaje.
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public Map<String, Object> ping(Principal principal) {
        log.debug("WebSocket ping de: {}", principal != null ? principal.getName() : "anonimo");
        return Map.of(
            "type", "PONG",
            "timestamp", LocalDateTime.now().toString(),
            "user", principal != null ? principal.getName() : "anonymous"
        );
    }

    /**
     * Broadcast de stats globales — usado para el dashboard en tiempo real.
     * Cualquier cliente suscrito a /topic/stats recibirá estas actualizaciones.
     */
    @MessageMapping("/stats")
    @SendTo("/topic/stats")
    public Map<String, Object> broadcastStats(Principal principal) {
        return Map.of(
            "type", "STATS_UPDATE",
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
