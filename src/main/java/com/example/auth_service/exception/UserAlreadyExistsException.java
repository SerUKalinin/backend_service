package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке регистрации пользователя с уже существующим именем.
 *
 * <p>Используется для предотвращения дублирования имени пользователя в системе.</p>
 *
 * <p>Возвращает HTTP статус 409 CONFLICT при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
