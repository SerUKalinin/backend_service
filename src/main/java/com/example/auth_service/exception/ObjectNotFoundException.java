package com.example.auth_service.exception;

/**
 * Исключение, выбрасываемое, если объект недвижимости не найден.
 */
public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String message) {
        super(message);
    }
}
