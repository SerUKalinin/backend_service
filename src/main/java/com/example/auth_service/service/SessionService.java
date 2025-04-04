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
     * В качестве значения сохраняем токен и время истечения.
     */
    public void saveSession(String username, String token, Duration duration) {
        String key = SESSION_PREFIX + username;
        log.info("Сохранение сессии для пользователя {} на {}", username, duration);

        // Сохраняем данные сессии
        redisTemplate.opsForHash().put(key, "token", token);
        redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));

        // Устанавливаем TTL для ключа сессии
        redisTemplate.expire(key, duration);
        log.info("Сессия пользователя {} успешно сохранена", username);
    }

    /**
     * Получает токен сессии пользователя из Redis.
     */
    public Optional<String> getSession(String username) {
        String key = SESSION_PREFIX + username;
        log.info("Получение сессии для пользователя {}", username);

        if (redisTemplate.hasKey(key)) {
            String token = (String) redisTemplate.opsForHash().get(key, "token");
            if (token != null) {
                log.info("Сессия пользователя {} найдена", username);
                return Optional.of(token);
            }
        }
        log.info("Сессия пользователя {} не найдена", username);
        return Optional.empty();
    }

    /**
     * Проверяет валидность сессии пользователя.
     */
    public boolean isSessionValid(String username, String token) {
        String key = SESSION_PREFIX + username;
        log.info("Проверка валидности сессии для пользователя {}", username);

        if (redisTemplate.hasKey(key)) {
            String storedToken = (String) redisTemplate.opsForHash().get(key, "token");
            boolean isValid = token.equals(storedToken);
            log.info("Сессия пользователя {} {}", username, isValid ? "валидна" : "невалидна");
            return isValid;
        }
        log.info("Сессия пользователя {} не найдена", username);
        return false;
    }

    /**
     * Проверяет истекла ли сессия пользователя.
     */
    public boolean isSessionExpired(String username) {
        String key = SESSION_PREFIX + username;
        log.info("Проверка истечения сессии для пользователя {}", username);

        if (redisTemplate.hasKey(key)) {
            String expiryTime = (String) redisTemplate.opsForHash().get(key, "expiry");
            if (expiryTime != null) {
                long expiryMillis = Long.parseLong(expiryTime);
                boolean isExpired = System.currentTimeMillis() > expiryMillis;
                log.info("Сессия пользователя {} {}", username, isExpired ? "истекла" : "активна");
                return isExpired;
            }
        }
        log.info("Сессия пользователя {} не найдена", username);
        return true;  // Если ключа нет, значит сессия истекла
    }

    /**
     * Обновляет время жизни сессии пользователя.
     */
    public void refreshSession(String username, Duration duration) {
        String key = SESSION_PREFIX + username;
        log.info("Обновление сессии для пользователя {} на {}", username, duration);

        if (redisTemplate.hasKey(key)) {
            String token = (String) redisTemplate.opsForHash().get(key, "token");
            if (token != null) {
                redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));
                redisTemplate.expire(key, duration);
                log.info("Сессия пользователя {} успешно обновлена", username);
            }
        } else {
            log.info("Сессия пользователя {} не найдена для обновления", username);
        }
    }

    /**
     * Удаляет сессию пользователя из Redis.
     */
    public void removeSession(String username) {
        String key = SESSION_PREFIX + username;
        log.info("Удаление сессии для пользователя {}", username);

        redisTemplate.delete(key);
        log.info("Сессия пользователя {} успешно удалена", username);
    }
}
