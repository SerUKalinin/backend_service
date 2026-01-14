package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Бизнес-исключение, возникающее при ошибках аутентификации или авторизации пользователя.
 *
 * <p>Используется для сигнализации о некорректных учетных данных, отсутствии прав доступа
 * или попытках выполнения защищенных операций без аутентификации.</p>
 *
 * <p>Аннотировано {@link ResponseStatus} со статусом {@link HttpStatus#UNAUTHORIZED},
 * что позволяет Spring автоматически возвращать HTTP 401 при выбрасывании данного исключения.</p>
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением.
     *
     * @param message подробное описание причины ошибки аутентификации или авторизации.
     */
    public AuthException(String message) {
        super(message);
    }
}
