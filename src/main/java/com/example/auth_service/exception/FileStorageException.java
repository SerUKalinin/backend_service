package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при ошибках хранения файлов.
 * Используется для общих ошибок, связанных с операциями чтения/записи файлов.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileStorageException extends RuntimeException {
    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение об ошибке
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением об ошибке и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина исключения
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
} 