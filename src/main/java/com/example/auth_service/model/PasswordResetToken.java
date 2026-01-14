package com.example.auth_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * Сущность для хранения токена сброса пароля в Redis.
 *
 * <p>Используется для временного хранения токенов, необходимых для подтверждения
 * операции восстановления пароля пользователя. Токены создаются при запросе сброса пароля
 * и валидны в течение ограниченного времени, управляемого логикой сервиса.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("password_reset_tokens")
public class PasswordResetToken {

    /**
     * Уникальный идентификатор токена в Redis.
     * Обычно формируется на основе email пользователя с префиксом для однозначной идентификации.
     */
    @Id
    private String id;

    /**
     * Токен сброса пароля.
     * Используется для проверки подлинности запроса на восстановление пароля.
     */
    private String token;
}
