package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при ошибках конфигурации Redis.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class RedisConfigurationException extends RuntimeException {
    public RedisConfigurationException(String message) {
        super(message);
    }
}