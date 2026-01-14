package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, связанное с ошибками аутентификации и авторизации пользователя.
 *
 * <p>Используется в случаях, когда пользователь не авторизован или
 * предоставленные учетные данные некорректны.</p>
 *
 * <p>Возвращает HTTP статус 401 UNAUTHORIZED при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public AuthException(String message) {
        super(message);
    }
}
