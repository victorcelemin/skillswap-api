package com.skillswap.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Wrapper genérico para todas las respuestas de la API.
 *
 * Decisión arquitectónica: usamos constructor manual aquí porque @Builder de Lombok
 * tiene limitaciones con clases genéricas en métodos estáticos factory.
 * Esta es la forma más robusta y clara de implementar el patrón envelope.
 *
 * @param <T> Tipo del dato en el campo data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta estandarizada de la API")
public class ApiResponse<T> {

    @Schema(description = "Indica si la operación fue exitosa")
    private boolean success;

    @Schema(description = "Mensaje descriptivo")
    private String message;

    @Schema(description = "Datos de la respuesta")
    private T data;

    @Schema(description = "Timestamp de la respuesta")
    private LocalDateTime timestamp;

    // Constructor privado — usar factory methods
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // ==================== FACTORY METHODS ====================

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // ==================== GETTERS ====================

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
