package com.skillswap.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción para errores de negocio (ej: créditos insuficientes, sesión ya confirmada).
 *
 * Decisión: tener una excepción de negocio genérica con status HTTP configurable
 * permite manejar distintos escenarios sin crear una clase por cada error.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
