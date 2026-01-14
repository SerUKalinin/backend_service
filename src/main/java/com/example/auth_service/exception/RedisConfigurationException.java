package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при ошибках конфигурации Redis.
 *
 * <p>Используется для сигнализации о проблемах с подключением или настройкой Redis,
 * которые могут повлиять на работу кэширования, хранения сессий и других компонентов,
 * зависящих от Redis.</p>
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class RedisConfigurationException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message описание ошибки конфигурации Redis, содержащее информацию о причине сбоя
     */
    public RedisConfigurationException(String message) {
        super(message);
    }
}
