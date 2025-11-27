package com.emijor.user_feed.utils.errors;

public class SimpleError extends Exception {
    private static final long serialVersionUID = 1L;

    private final int statusCode;
    private final String error;

    public SimpleError(int statusCode, String error) {
        super(error);
        this.statusCode = statusCode;
        this.error = error;
    }

    public SimpleError(String error) {
        super(error);
        this.statusCode = 500;
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }
}
