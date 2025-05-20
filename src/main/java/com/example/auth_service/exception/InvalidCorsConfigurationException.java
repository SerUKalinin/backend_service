package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при некорректной конфигурации CORS.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidCorsConfigurationException extends RuntimeException {
    public InvalidCorsConfigurationException(String message) {
        super(message);
    }
}
