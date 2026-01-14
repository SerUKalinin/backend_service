package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при превышении установленного лимита запросов к серверу.
 *
 * <p>Используется для защиты системы от перегрузки и ограничения частоты обращений клиентов.</p>
 *
 * <p>Возвращает HTTP статус 429 TOO MANY REQUESTS при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public RateLimitExceededException(String message) {
        super(message);
    }
}
