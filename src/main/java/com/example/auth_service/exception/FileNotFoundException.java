package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке получить доступ к файлу, которого нет в хранилище.
 *
 * <p>Используется для сигнализации о том, что запрашиваемый файл отсутствует или недоступен.
 * Позволяет централизованно обрабатывать ошибки отсутствия файлов в системе.</p>
 *
 * <p>Аннотировано {@link ResponseStatus} со статусом {@link HttpStatus#NOT_FOUND},
 * что позволяет Spring автоматически возвращать HTTP 404 при выбрасывании данного исключения.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message подробное описание причины возникновения исключения.
     */
    public FileNotFoundException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением и причиной возникновения.
     *
     * @param message подробное описание причины возникновения исключения.
     * @param cause причина возникновения исключения, может быть null.
     */
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
