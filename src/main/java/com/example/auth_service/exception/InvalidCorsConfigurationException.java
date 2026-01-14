package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при некорректной конфигурации CORS (Cross-Origin Resource Sharing).
 *
 * <p>Используется для сигнализации о проблемах с настройками CORS,
 * которые могут препятствовать корректной работе фронтенда с API.</p>
 *
 * <p>Возвращает HTTP статус 500 INTERNAL SERVER ERROR при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidCorsConfigurationException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public InvalidCorsConfigurationException(String message) {
        super(message);
    }
}
