package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при ошибках работы с файловым хранилищем.
 *
 * <p>Используется для обработки общих ошибок, связанных с операциями
 * чтения, записи или сохранения файлов.</p>
 *
 * <p>Возвращает HTTP статус 500 INTERNAL SERVER ERROR при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileStorageException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением об ошибке и причиной.
     *
     * @param message сообщение, описывающее причину исключения
     * @param cause причина, вызвавшая исключение
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
