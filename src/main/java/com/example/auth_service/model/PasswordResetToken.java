package com.example.auth_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * Модель для хранения токена сброса пароля в Redis.
 *
 * <p>Используется для временного хранения токена, который позволяет пользователю
 * сбросить пароль по ссылке, отправленной на email.</p>
 *
 * <p>Ключ в Redis формируется на основе email пользователя с префиксом.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("password_reset_tokens")
public class PasswordResetToken {

    /**
     * Уникальный идентификатор записи в Redis.
     * Обычно формируется как email пользователя с префиксом.
     */
    @Id
    private String id;

    /**
     * Токен для сброса пароля.
     * Используется для подтверждения права пользователя изменить пароль.
     */
    private String token;
}
