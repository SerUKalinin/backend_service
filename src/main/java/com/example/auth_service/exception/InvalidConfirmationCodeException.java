package com.example.auth_service.exception;

/**
 * Исключение, возникающее при неверном коде подтверждения.
 */
public class InvalidConfirmationCodeException extends RuntimeException {
    public InvalidConfirmationCodeException(String message) {
        super(message);
    }
}
