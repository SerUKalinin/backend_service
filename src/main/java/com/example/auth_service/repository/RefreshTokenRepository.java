package com.example.auth_service.repository;

import com.example.auth_service.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностями {@link RefreshToken}.
 * <p>
 * Предоставляет стандартные CRUD-операции через {@link JpaRepository}
 * и дополнительные методы для поиска и удаления токенов по значению токена.
 * </p>
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Находит токен обновления по его значению.
     *
     * @param token значение токена
     * @return {@link Optional} с сущностью {@link RefreshToken}, если найдено, иначе пустой
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Удаляет токен обновления по его значению.
     *
     * @param token значение токена
     */
    void deleteByToken(String token);
}
