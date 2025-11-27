package com.emijor.user_feed.utils.errors;

public class ForbiddenError extends SimpleError {
    private static final long serialVersionUID = 1L;

    public ForbiddenError() {
        super(403, "Forbidden");
    }

    public ForbiddenError(String message) {
        super(403, message);
    }
}
