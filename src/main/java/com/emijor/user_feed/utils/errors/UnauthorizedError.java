package com.emijor.user_feed.utils.errors;

/**
 * Error de autorización (401 Unauthorized).
 * Se lanza cuando el usuario no está autenticado o el token es inválido.
 */
public class UnauthorizedError extends SimpleError {
    private static final long serialVersionUID = 1L;

    public UnauthorizedError() {
        super(401, "Unauthorized");
    }

    public UnauthorizedError(String message) {
        super(401, message);
    }
}
