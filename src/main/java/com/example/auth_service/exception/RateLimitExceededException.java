package com.example.auth_service.exception;

/**
 * Исключение, выбрасываемое при превышении лимита запросов.
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
} 