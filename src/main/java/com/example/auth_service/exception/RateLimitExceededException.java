package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при превышении лимита запросов к системе.
 *
 * <p>Используется для защиты от чрезмерного числа запросов от одного пользователя или клиента.
 * Генерируется, когда количество запросов превышает установленный лимит в заданный интервал времени.</p>
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message описание ошибки, содержащее информацию о причине превышения лимита
     */
    public RateLimitExceededException(String message) {
        super(message);
    }
}
