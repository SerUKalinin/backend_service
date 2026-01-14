package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке загрузки файла, не соответствующего требованиям системы.
 *
 * <p>Применяется при нарушении правил валидации файла, таких как:
 * неподдерживаемый тип файла, превышение допустимого размера или некорректное имя файла.</p>
 *
 * <p>Возвращает HTTP статус 400 BAD REQUEST при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFileException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public InvalidFileException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением об ошибке и причиной.
     *
     * @param message сообщение, описывающее причину исключения
     * @param cause причина, вызвавшая исключение
     */
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
