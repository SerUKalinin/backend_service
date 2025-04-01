package com.example.auth_service.exception;

/**
 * Исключение, если введены некорректные данные.
 */
public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }
}
