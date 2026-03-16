package com.skillswap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para recursos no encontrados (404).
 * Decisión: @ResponseStatus en la excepción como documentación,
 * aunque el manejo real lo hace el GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " con ID " + id + " no encontrado");
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(resource + " con " + field + " = " + value + " no encontrado");
    }
}
