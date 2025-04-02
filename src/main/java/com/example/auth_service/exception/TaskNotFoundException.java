package com.example.auth_service.exception;

/**
 * Исключение, выбрасываемое, если задача не найдена.
 */
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
