package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, которое выбрасывается при попытке зарегистрировать пользователя с уже существующим email.
 *
 * <p>Используется для предотвращения дублирования учетных записей и обеспечения уникальности
 * email в системе.</p>
 *
 * <p>Аннотировано {@link ResponseStatus} со статусом {@link HttpStatus#CONFLICT},
 * что позволяет Spring автоматически возвращать HTTP 409 при выбрасывании данного исключения.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением.
     *
     * @param message подробное описание причины возникновения исключения.
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
