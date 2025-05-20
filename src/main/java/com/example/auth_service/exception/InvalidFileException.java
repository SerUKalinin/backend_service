package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке загрузки невалидного файла.
 * Используется при нарушении правил валидации файла (размер, тип, имя и т.д.).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFileException extends RuntimeException {
    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение об ошибке
     */
    public InvalidFileException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением об ошибке и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина исключения
     */
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
} 