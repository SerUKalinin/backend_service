package com.example.auth_service.service;

import com.example.auth_service.repository.redis.RedisSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Сервис для управления сессиями пользователей.
 * <p>
 * Сохраняет, проверяет, обновляет и удаляет сессии пользователей в Redis через {@link RedisSessionRepository}.
 * Сессии содержат токен пользователя и время жизни сессии.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final RedisSessionRepository redisSessionRepository;

    /**
     * Сохраняет сессию пользователя в Redis.
     * <p>
     * В качестве значения сохраняется токен и время истечения сессии.
     *
     * @param username имя пользователя
     * @param token токен сессии
     * @param duration время жизни сессии
     */
    public void saveSession(String username, String token, Duration duration) {
        log.debug("Сохранение сессии для пользователя {} на {}", username, duration);
        redisSessionRepository.saveSession(username, token, duration);
    }

    /**
     * Проверяет валидность сессии пользователя.
     * <p>
     * Сессия считается невалидной, если она отсутствует или истекла.
     *
     * @param username имя пользователя
     * @param token токен сессии
     * @return {@code true}, если сессия существует и не истекла; {@code false} иначе
     */
    public boolean isSessionValid(String username, String token) {
        if (!redisSessionRepository.isSessionExists(username, token)
                || redisSessionRepository.isSessionExpired(username, token)) {

            log.warn("Сессия пользователя {} невалидна", username);
            return false;
        }

        return true;
    }

    /**
     * Обновляет время жизни существующей сессии пользователя.
     *
     * @param username имя пользователя
     * @param token токен сессии
     * @param duration новое время жизни сессии
     */
    public void updateSession(String username, String token, Duration duration) {
        log.debug("Обновление сессии для пользователя {} на {}", username, duration);
        redisSessionRepository.updateSession(username, token, duration);
    }

    /**
     * Удаляет сессию пользователя из Redis.
     *
     * @param username имя пользователя
     */
    public void removeSession(String username) {
        log.debug("Удаление сессии пользователя {}", username);
        redisSessionRepository.removeSession(username);
    }
}
