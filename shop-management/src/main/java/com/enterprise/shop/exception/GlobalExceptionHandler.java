package com.enterprise.shop.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for Shop Management Service
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        Map<String, Object> response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                fieldErrors.toString()
        );
        response.put("fieldErrors", fieldErrors);

        logger.warn("Validation error: {}", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleWebClientErrors(WebClientResponseException ex) {
        logger.error("Stock service communication error: {} - {}", ex.getStatusCode(), ex.getMessage());
        
        Map<String, Object> response = buildErrorResponse(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                "Stock Service Error",
                ex.getResponseBodyAsString()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage()
        );

        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected Error",
                "An unexpected error occurred. Please try again later."
        );

        return ResponseEntity.internalServerError().body(response);
    }

    private Map<String, Object> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        response.put("service", "shop-management");
        return response;
    }
}
