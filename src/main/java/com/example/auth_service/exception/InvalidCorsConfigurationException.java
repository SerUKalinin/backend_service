package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при некорректной конфигурации CORS в приложении.
 *
 * <p>Используется для сигнализации о нарушении правил кросс-доменных запросов,
 * которые могут препятствовать корректной работе фронтенд-клиентов.</p>
 *
 * <p>Возникает при старте приложения или при динамическом обновлении CORS-настроек,
 * если конфигурация содержит недопустимые значения.</p>
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidCorsConfigurationException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message описание ошибки, указывающее на причину некорректной CORS-конфигурации
     */
    public InvalidCorsConfigurationException(String message) {
        super(message);
    }
}
