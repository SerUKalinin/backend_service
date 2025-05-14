package com.example.auth_service.service;

import com.example.auth_service.repository.redis.RedisSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final RedisSessionRepository redisSessionRepository;

    /**
     * Сохраняет данные сессии пользователя в Redis.
     * В качестве значения сохраняем токен и время истечения.
     */
    public void saveSession(String username, String token, Duration duration) {
        log.debug("Сохранение сессии для пользователя {} на {}", username, duration);
        redisSessionRepository.saveSession(username, token, duration);
    }

    /**
     * Получает токен сессии пользователя из Redis.
     */
    public boolean isSessionValid(String username, String token) {
        boolean isValid = redisSessionRepository.isSessionExists(username, token) && 
                         !redisSessionRepository.isSessionExpired(username, token);
        if (!isValid) {
            log.warn("Сессия пользователя {} невалидна", username);
        }
        return isValid;
    }

    /**
     * Обновляет время жизни сессии пользователя.
     */
    public void updateSession(String username, String token, Duration duration) {
        log.debug("Обновление сессии для пользователя {} на {}", username, duration);
        redisSessionRepository.updateSession(username, token, duration);
    }

    /**
     * Удаляет сессию пользователя из Redis.
     */
    public void removeSession(String username) {
        log.debug("Удаление сессии пользователя {}", username);
        redisSessionRepository.removeSession(username);
    }
}
