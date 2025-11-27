package com.emijor.user_feed.utils.errors;

public class NotFoundError extends SimpleError {
    private static final long serialVersionUID = 1L;

    public NotFoundError() {
        super(404, "Not Found");
    }

    public NotFoundError(String message) {
        super(404, message);
    }
}
