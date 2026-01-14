package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при предоставлении некорректных или недопустимых данных.
 *
 * <p>Используется для сигнализации о валидационных ошибках или ошибках формата данных,
 * которые не соответствуют требованиям системы.</p>
 *
 * <p>Возвращает HTTP статус 400 BAD REQUEST при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDataException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public InvalidDataException(String message) {
        super(message);
    }
}
