package com.example.auth_service.exception;

/**
 * Исключение, выбрасываемое, если задача с таким названием уже существует.
 */
public class TaskAlreadyExistsException extends RuntimeException {
    public TaskAlreadyExistsException(String message) {
        super(message);
    }
}
