package com.skillswap.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para créditos insuficientes.
 * Extiende BusinessException para tratamiento uniforme.
 */
public class InsufficientCreditsException extends BusinessException {

    public InsufficientCreditsException(int required, int available) {
        super(
            String.format(
                "Créditos insuficientes. Necesitas %d créditos pero tienes %d disponibles.",
                required, available
            ),
            HttpStatus.PAYMENT_REQUIRED
        );
    }
}
