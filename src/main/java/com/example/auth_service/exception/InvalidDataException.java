package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке обработки некорректных данных, переданных пользователем.
 *
 * <p>Используется для сигнализации о нарушении бизнес-правил или валидации на уровне DTO,
 * когда данные не соответствуют ожидаемому формату, ограничениям или требованиям системы.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDataException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message описание ошибки, поясняющее, какие данные были некорректными
     */
    public InvalidDataException(String message) {
        super(message);
    }
}
