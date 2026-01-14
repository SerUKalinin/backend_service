package com.example.auth_service.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * Репозиторий для управления сессиями пользователей в Redis.
 * <p>
 * Использует {@link StringRedisTemplate} для хранения токенов сессий и их времени жизни.
 * Каждая сессия хранится как Hash с ключом вида "session:{username}".
 * </p>
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisSessionRepository {

    private final StringRedisTemplate redisTemplate;

    /** Префикс ключей сессий в Redis. */
    private static final String SESSION_PREFIX = "session:";

    /**
     * Сохраняет новую сессию пользователя в Redis.
     *
     * @param username имя пользователя, для которого создаётся сессия
     * @param token токен сессии
     * @param duration длительность жизни сессии
     */
    public void saveSession(String username, String token, Duration duration) {
        String key = SESSION_PREFIX + username;
        redisTemplate.opsForHash().put(key, "token", token);
        redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));
        redisTemplate.expire(key, duration);
    }

    /**
     * Проверяет, существует ли активная сессия для пользователя с указанным токеном.
     *
     * @param username имя пользователя
     * @param token токен сессии
     * @return true, если сессия существует и токен совпадает, иначе false
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
     * @param username имя пользователя
     * @param token токен сессии
     * @return true, если сессия истекла или не найдена, иначе false
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
     * @param username имя пользователя
     * @param token токен сессии
     * @param duration новая длительность жизни сессии
     */
    public void updateSession(String username, String token, Duration duration) {
        String key = SESSION_PREFIX + username;
        redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));
        redisTemplate.expire(key, duration);
    }

    /**
     * Удаляет сессию пользователя из Redis.
     *
     * @param username имя пользователя, чья сессия удаляется
     */
    public void removeSession(String username) {
        String key = SESSION_PREFIX + username;
        redisTemplate.delete(key);
    }
}
