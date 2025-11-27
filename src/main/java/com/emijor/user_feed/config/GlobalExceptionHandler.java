package com.emijor.user_feed.config;

import com.emijor.user_feed.utils.errors.BadRequestError;
import com.emijor.user_feed.utils.errors.ForbiddenError;
import com.emijor.user_feed.utils.errors.NotFoundError;
import com.emijor.user_feed.utils.errors.SimpleError;
import com.emijor.user_feed.utils.errors.UnauthorizedError;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedError.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedError(UnauthorizedError ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getError());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingHeader(MissingRequestHeaderException ex) {
        Map<String, String> error = new HashMap<>();
        if ("Authorization".equalsIgnoreCase(ex.getHeaderName())) {
            error.put("error", "Unauthorized");
        } else {
            error.put("error", "Header requerido: " + ex.getHeaderName());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadRequestError.class)
    public ResponseEntity<Map<String, String>> handleBadRequestError(BadRequestError ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ForbiddenError.class)
    public ResponseEntity<Map<String, String>> handleForbiddenError(ForbiddenError ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getError());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(NotFoundError.class)
    public ResponseEntity<Map<String, String>> handleNotFoundError(NotFoundError ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getError());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(SimpleError.class)
    public ResponseEntity<Map<String, String>> handleSimpleError(SimpleError ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getError());
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> 
            errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        
        response.put("error", "Validation failed");
        response.put("details", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Recurso no encontrado");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error interno del servidor");
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
