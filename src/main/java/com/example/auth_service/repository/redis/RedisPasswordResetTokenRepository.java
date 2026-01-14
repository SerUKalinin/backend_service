package com.example.auth_service.repository.redis;

import com.example.auth_service.model.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для управления токенами сброса пароля в Redis.
 *
 * <p>Обеспечивает CRUD-операции для {@link PasswordResetToken}, включая сохранение,
 * получение, обновление и удаление токенов. Используется для реализации
 * функционала восстановления пароля пользователей с хранением токенов в Redis.</p>
 *
 * <p>Все стандартные методы CRUD наследуются от {@link CrudRepository}:
 * save, findById, existsById, findAll, count, deleteById, delete и т.д.</p>
 */
@Repository
public interface RedisPasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {
}
