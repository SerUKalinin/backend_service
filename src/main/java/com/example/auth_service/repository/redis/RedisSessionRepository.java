package com.example.auth_service.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * Репозиторий для управления сессиями пользователей в Redis.
 *
 * <p>Обеспечивает сохранение, проверку, обновление и удаление сессий пользователей.
 * Каждая сессия хранится как hash с полями token и expiry, где expiry — время истечения сессии.</p>
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisSessionRepository {

    /** RedisTemplate для работы с ключами типа String и hash-структурами. */
    private final StringRedisTemplate redisTemplate;

    /** Префикс ключей сессий для уникальной идентификации в Redis. */
    private static final String SESSION_PREFIX = "session:";

    /**
     * Сохраняет новую сессию пользователя в Redis с указанным токеном и временем жизни.
     *
     * @param username Имя пользователя. Не может быть null или пустым.
     * @param token Токен сессии. Не может быть null или пустым.
     * @param duration Время жизни сессии. Не может быть null.
     */
    public void saveSession(String username, String token, Duration duration) {
        String key = SESSION_PREFIX + username;
        redisTemplate.opsForHash().put(key, "token", token);
        redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));
        redisTemplate.expire(key, duration);
    }

    /**
     * Проверяет существование сессии для пользователя и совпадение токена.
     *
     * @param username Имя пользователя. Не может быть null.
     * @param token Токен сессии для проверки. Не может быть null.
     * @return true, если сессия существует и токен совпадает, иначе false.
     */
    public boolean isSessionExists(String username, String token) {
        String key = SESSION_PREFIX + username;
        if (!redisTemplate.hasKey(key)) {
            log.debug("Сессия не найдена для пользователя: {}", username);
            return false;
        }
        String storedToken = (String) redisTemplate.opsForHash().get(key, "token");
        return token.equals(storedToken);
    }

    /**
     * Проверяет, истекла ли сессия пользователя.
     *
     * @param username Имя пользователя. Не может быть null.
     * @param token Токен сессии. Не используется для проверки, но необходим для согласованного интерфейса.
     * @return true, если сессия истекла или не найдена, иначе false.
     */
    public boolean isSessionExpired(String username, String token) {
        String key = SESSION_PREFIX + username;
        String expiryTime = (String) redisTemplate.opsForHash().get(key, "expiry");
        if (expiryTime == null) {
            log.debug("Время истечения не найдено для пользователя: {}", username);
            return true;
        }
        boolean isExpired = System.currentTimeMillis() > Long.parseLong(expiryTime);
        if (isExpired) {
            log.debug("Сессия истекла для пользователя: {}", username);
        }
        return isExpired;
    }

    /**
     * Обновляет время жизни существующей сессии пользователя.
     *
     * @param username Имя пользователя. Не может быть null.
     * @param token Токен сессии. Не используется для обновления, но требуется для согласованного интерфейса.
     * @param duration Новая длительность сессии. Не может быть null.
     */
    public void updateSession(String username, String token, Duration duration) {
        String key = SESSION_PREFIX + username;
        redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));
        redisTemplate.expire(key, duration);
    }

    /**
     * Удаляет сессию пользователя из Redis.
     *
     * @param username Имя пользователя. Не может быть null.
     */
    public void removeSession(String username) {
        String key = SESSION_PREFIX + username;
        redisTemplate.delete(key);
    }
}
