package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке доступа к несуществующему файлу.
 *
 * <p>Используется, когда запрашиваемый файл не найден в хранилище или недоступен.</p>
 *
 * <p>Возвращает HTTP статус 404 NOT FOUND при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public FileNotFoundException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением об ошибке и причиной.
     *
     * @param message сообщение, описывающее причину исключения
     * @param cause причина, вызвавшая исключение
     */
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
