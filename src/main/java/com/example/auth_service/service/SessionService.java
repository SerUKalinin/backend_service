package com.example.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final StringRedisTemplate redisTemplate;

    private static final String SESSION_PREFIX = "session:";

    /**
     * Сохраняет данные сессии пользователя в Redis.
     * В качестве значения сохраняем токен, но можно расширить для других данных.
     */
    public void saveSession(String username, String token, Duration duration) {
        String key = SESSION_PREFIX + username;

        // Сохраняем данные сессии как строки
        redisTemplate.opsForHash().put(key, "token", token);
        redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));

        // Устанавливаем TTL для ключа сессии
        redisTemplate.expire(key, duration);
        log.info("Сессия пользователя {} сохранена в Redis на {}", username, duration);
    }

    /**
     * Получает токен сессии пользователя из Redis.
     */
    public Optional<String> getSession(String username) {
        String key = SESSION_PREFIX + username;

        if (redisTemplate.hasKey(key)) {
            // Приводим объект, возвращаемый Redis, к типу String
            String token = (String) redisTemplate.opsForHash().get(key, "token");
            return Optional.ofNullable(token);
        }
        return Optional.empty();
    }

    /**
     * Проверяет истекла ли сессия пользователя.
     */
    public boolean isSessionExpired(String username) {
        String key = SESSION_PREFIX + username;

        if (redisTemplate.hasKey(key)) {
            // Приводим объект, возвращаемый Redis, к типу String
            String expiryTime = (String) redisTemplate.opsForHash().get(key, "expiry");
            long expiryMillis = Long.parseLong(expiryTime);
            return System.currentTimeMillis() > expiryMillis;
        }
        return true;  // Если ключа нет, значит сессия истекла
    }

    /**
     * Удаляет сессию пользователя из Redis.
     */
    public void removeSession(String username) {
        String key = SESSION_PREFIX + username;
        redisTemplate.delete(key);
        log.info("Сессия пользователя {} удалена из Redis", username);
    }
}
