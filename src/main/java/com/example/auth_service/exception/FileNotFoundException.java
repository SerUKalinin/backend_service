package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке доступа к несуществующему файлу.
 * Используется, когда запрашиваемый файл не найден в хранилище.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileNotFoundException extends RuntimeException {
    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение об ошибке
     */
    public FileNotFoundException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением об ошибке и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина исключения
     */
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 