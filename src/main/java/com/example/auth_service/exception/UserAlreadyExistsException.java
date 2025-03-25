package com.example.auth_service.exception;

/**
 * Исключение, выбрасываемое, если пользователь с таким именем уже существует.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
