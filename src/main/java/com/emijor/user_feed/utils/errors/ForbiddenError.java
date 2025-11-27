package com.emijor.user_feed.utils.errors;

/**
 * Error de acceso prohibido (403 Forbidden).
 * Se lanza cuando el usuario está autenticado pero no tiene permisos suficientes.
 */
public class ForbiddenError extends SimpleError {
    private static final long serialVersionUID = 1L;

    public ForbiddenError() {
        super(403, "Forbidden");
    }

    public ForbiddenError(String message) {
        super(403, message);
    }
}
