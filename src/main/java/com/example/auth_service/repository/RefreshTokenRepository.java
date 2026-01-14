package com.example.auth_service.repository;

import com.example.auth_service.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Репозиторий для управления сущностями обновляющих токенов (RefreshToken).
 * Предоставляет методы для поиска токенов по значению и удаления токенов из базы данных.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Находит сущность RefreshToken по значению токена.
     *
     * @param token Строковое значение токена. Не может быть null или пустым.
     * @return Optional с найденным RefreshToken. Пустой Optional, если токен не найден.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Удаляет сущность RefreshToken по значению токена.
     *
     * @param token Строковое значение токена. Не может быть null или пустым.
     */
    void deleteByToken(String token);
}
