package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке загрузки файла, не соответствующего требованиям системы.
 *
 * <p>Используется для сигнализации о нарушении правил валидации файлов, таких как размер, тип, имя или другие ограничения,
 * установленные в приложении.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFileException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message описание ошибки, поясняющее, почему файл считается некорректным
     */
    public InvalidFileException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением об ошибке и причиной.
     *
     * @param message описание ошибки
     * @param cause причина возникновения исключения, например, системная ошибка при обработке файла
     */
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
