package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке регистрации пользователя с именем,
 * которое уже существует в системе.
 *
 * <p>Используется для предотвращения дублирования учетных записей и информирования
 * сервисного и пользовательского слоев о конфликте данных.
 * Возвращает HTTP-статус {@link HttpStatus#CONFLICT}.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message подробное описание причины ошибки, например, информация о существующем имени пользователя
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
