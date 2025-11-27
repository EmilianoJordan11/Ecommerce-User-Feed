package com.emijor.user_feed.utils.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestError extends Exception {
    
    public BadRequestError(String message) {
        super(message);
    }
}
