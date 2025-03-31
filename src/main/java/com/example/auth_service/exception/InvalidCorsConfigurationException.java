package com.example.auth_service.exception;

/**
 * Исключение, возникающее при некорректной конфигурации CORS.
 */
public class InvalidCorsConfigurationException extends RuntimeException {
    public InvalidCorsConfigurationException(String message) {
        super(message);
    }
}
