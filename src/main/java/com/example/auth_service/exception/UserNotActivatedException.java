package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке входа пользователя, который не прошёл активацию.
 *
 * <p>Используется для блокировки доступа неактивированных пользователей к системе.</p>
 *
 * <p>Возвращает HTTP статус 403 FORBIDDEN при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserNotActivatedException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public UserNotActivatedException(String message) {
        super(message);
    }
}
