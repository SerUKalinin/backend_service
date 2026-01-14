package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при попытке доступа к пользователю,
 * который отсутствует в системе.
 *
 * <p>Используется для информирования сервисного слоя и клиентов API о том,
 * что запрашиваемый пользователь не найден в базе данных.
 * Возвращает HTTP-статус {@link HttpStatus#NOT_FOUND}.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message описание причины ошибки, например, идентификатор или имя пользователя, который не найден
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
