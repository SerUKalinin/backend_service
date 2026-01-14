package com.example.auth_service.service;

import com.example.auth_service.repository.redis.RedisSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Сервис управления сессиями пользователей.
 *
 * <p>Отвечает за сохранение, проверку валидности, обновление и удаление сессий
 * в Redis. Используется для управления авторизационными токенами и контроля
 * времени жизни сессий.</p>
 *
 * <p>Сервис не выполняет аутентификацию, а работает только с хранением и валидацией сессий.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    /** Репозиторий для работы с сессиями в Redis. */
    private final RedisSessionRepository redisSessionRepository;

    /**
     * Сохраняет данные сессии пользователя в Redis.
     *
     * @param username имя пользователя, для которого сохраняется сессия
     * @param token токен сессии
     * @param duration время жизни сессии; после истечения токен станет невалидным
     */
    public void saveSession(String username, String token, Duration duration) {
        log.debug("Сохранение сессии для пользователя {} на {}", username, duration);
        redisSessionRepository.saveSession(username, token, duration);
    }

    /**
     * Проверяет валидность сессии пользователя.
     *
     * <p>Сессия считается невалидной, если отсутствует в Redis или истек срок её действия.</p>
     *
     * @param username имя пользователя
     * @param token токен сессии для проверки
     * @return {@code true}, если сессия существует и ещё действительна; {@code false} в противном случае
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
     * @param username имя пользователя, чья сессия будет удалена
     */
    public void removeSession(String username) {
        log.debug("Удаление сессии пользователя {}", username);
        redisSessionRepository.removeSession(username);
    }
}
