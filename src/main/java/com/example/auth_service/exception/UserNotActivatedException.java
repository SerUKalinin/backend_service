package com.example.auth_service.exception;

/**
 * Исключение, возникающее при попытке входа неактивированного пользователя.
 */
public class UserNotActivatedException extends RuntimeException {
    public UserNotActivatedException(String message) {
        super(message);
    }
}
