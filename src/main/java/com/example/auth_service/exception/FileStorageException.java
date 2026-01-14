package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при ошибках операций с файлами в системе.
 *
 * <p>Используется для сигнализации о проблемах при чтении, записи или обработке файлов.
 * Позволяет централизованно обрабатывать ошибки файлового хранилища.</p>
 *
 * <p>Аннотировано {@link ResponseStatus} со статусом {@link HttpStatus#INTERNAL_SERVER_ERROR},
 * что позволяет Spring автоматически возвращать HTTP 500 при выбрасывании данного исключения.</p>
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileStorageException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message подробное описание причины возникновения исключения.
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением и причиной возникновения.
     *
     * @param message подробное описание причины возникновения исключения.
     * @param cause причина возникновения исключения, может быть null.
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
