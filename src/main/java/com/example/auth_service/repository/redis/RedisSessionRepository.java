package com.example.auth_service.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisSessionRepository {

    private final StringRedisTemplate redisTemplate;
    private static final String SESSION_PREFIX = "session:";

    public void saveSession(String username, String token, Duration duration) {
        String key = SESSION_PREFIX + username;
        redisTemplate.opsForHash().put(key, "token", token);
        redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));
        redisTemplate.expire(key, duration);
    }

    public boolean isSessionExists(String username, String token) {
        String key = SESSION_PREFIX + username;
        if (!redisTemplate.hasKey(key)) {
            log.debug("Сессия не найдена для пользователя: {}", username);
            return false;
        }
        String storedToken = (String) redisTemplate.opsForHash().get(key, "token");
        return token.equals(storedToken);
    }

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

    public void updateSession(String username, String token, Duration duration) {
        String key = SESSION_PREFIX + username;
        redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));
        redisTemplate.expire(key, duration);
    }

    public void removeSession(String username) {
        String key = SESSION_PREFIX + username;
        redisTemplate.delete(key);
    }
} 