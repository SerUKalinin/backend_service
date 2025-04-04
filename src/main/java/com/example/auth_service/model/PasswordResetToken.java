package com.example.auth_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * Модель для хранения токена сброса пароля в Redis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("password_reset_tokens")
public class PasswordResetToken {
    @Id
    private String id; // email пользователя с префиксом
    private String token;
} 