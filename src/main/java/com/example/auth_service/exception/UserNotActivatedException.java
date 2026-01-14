package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке аутентификации пользователя,
 * который зарегистрирован в системе, но еще не активирован.
 *
 * <p>Используется для информирования сервисного слоя и фронтенда о том,
 * что учетная запись существует, но не разрешена к входу.
 * Возвращает HTTP-статус {@link HttpStatus#FORBIDDEN}.</p>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserNotActivatedException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message описание причины ошибки, например, уведомление о том, что пользователь не активирован
     */
    public UserNotActivatedException(String message) {
        super(message);
    }
}
