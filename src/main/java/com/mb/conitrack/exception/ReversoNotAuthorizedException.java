package com.mb.conitrack.exception;

/**
 * Excepción lanzada cuando un usuario intenta reversar un movimiento sin autorización.
 *
 * Reglas de autorización:
 * - Solo el usuario que creó el movimiento puede reversarlo
 * - Usuarios con nivel jerárquico superior también pueden reversar
 * - Usuarios con mismo nivel o inferior NO pueden reversar (excepto el creador)
 */
public class ReversoNotAuthorizedException extends RuntimeException {

    public ReversoNotAuthorizedException(String message) {
        super(message);
    }

    public ReversoNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
