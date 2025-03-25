package com.example.auth_service.exception;

/**
 * Исключение, выбрасываемое при ошибках конфигурации Redis.
 */
public class RedisConfigurationException extends RuntimeException {
    public RedisConfigurationException(String message) {
        super(message);
    }
}