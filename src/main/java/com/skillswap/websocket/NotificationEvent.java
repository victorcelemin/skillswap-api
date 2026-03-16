package com.skillswap.websocket;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para eventos de notificación en tiempo real via WebSocket.
 *
 * Decisión: usamos un DTO genérico con un campo 'data' tipado como Object
 * en lugar de un DTO por evento. Esto simplifica el código del cliente
 * (frontend) que solo necesita deserializar un tipo de mensaje.
 */
@Data
@Builder
public class NotificationEvent {

    public enum EventType {
        SESSION_BOOKED,      // Nuevo estudiante reservó tu sesión
        SESSION_CONFIRMED,   // Tu teacher confirmó la sesión
        SESSION_COMPLETED,   // Sesión completada — créditos transferidos
        SESSION_CANCELLED,   // Sesión cancelada — créditos reembolsados
        REVIEW_RECEIVED,     // Recibiste una nueva review
        CREDITS_UPDATED      // Tu saldo de créditos cambió
    }

    private EventType type;
    private String title;
    private String message;
    private Object data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ==================== FACTORY METHODS ====================

    public static NotificationEvent sessionBooked(String teacherUsername, Object sessionData) {
        return NotificationEvent.builder()
            .type(EventType.SESSION_BOOKED)
            .title("Nueva sesion reservada!")
            .message("Un estudiante ha reservado tu sesion")
            .data(sessionData)
            .build();
    }

    public static NotificationEvent sessionConfirmed(Object sessionData) {
        return NotificationEvent.builder()
            .type(EventType.SESSION_CONFIRMED)
            .title("Sesion confirmada!")
            .message("Tu teacher ha confirmado la sesion")
            .data(sessionData)
            .build();
    }

    public static NotificationEvent sessionCompleted(int creditsEarned, Object sessionData) {
        return NotificationEvent.builder()
            .type(EventType.SESSION_COMPLETED)
            .title("Sesion completada!")
            .message("Tu sesion ha sido completada. +" + creditsEarned + " creditos recibidos!")
            .data(sessionData)
            .build();
    }
}
