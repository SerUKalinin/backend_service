package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при ошибках конфигурации Redis в приложении.
 *
 * <p>Используется для сигнализации о проблемах с подключением или настройками Redis,
 * необходимых для работы кэша или сессий.</p>
 *
 * <p>Возвращает HTTP статус 500 INTERNAL SERVER ERROR при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class RedisConfigurationException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public RedisConfigurationException(String message) {
        super(message);
    }
}
