package com.example.auth_service.exception;

/**
 * Исключение, которое выбрасывается, когда попытка регистрации пользователя с уже существующим email.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
