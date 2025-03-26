package com.example.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String SESSION_PREFIX = "session:";

    /**
     * Сохраняет токен сессии в Redis.
     */
    public void saveSession(String username, String token, Duration duration) {
        String key = SESSION_PREFIX + username;
        redisTemplate.opsForValue().set(key, token, duration);
        log.info("Сессия пользователя {} сохранена в Redis на {}", username, duration);
    }

    /**
     * Получает токен сессии пользователя.
     */
    public Optional<String> getSession(String username) {
        String key = SESSION_PREFIX + username;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    /**
     * Удаляет сессию пользователя.
     */
    public void removeSession(String username) {
        String key = SESSION_PREFIX + username;
        redisTemplate.delete(key);
        log.info("Сессия пользователя {} удалена из Redis", username);
    }
}
