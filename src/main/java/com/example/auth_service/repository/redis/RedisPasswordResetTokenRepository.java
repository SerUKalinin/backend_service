package com.example.auth_service.repository.redis;

import com.example.auth_service.model.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для управления токенами сброса пароля в Redis.
 */
@Repository
public interface RedisPasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {
    // Базовые методы CRUD наследуются от CrudRepository
} 