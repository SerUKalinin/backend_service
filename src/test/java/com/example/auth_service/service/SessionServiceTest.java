package com.example.auth_service.service;

import com.example.auth_service.repository.redis.RedisSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SessionServiceTest {

    private RedisSessionRepository redisSessionRepository;
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        redisSessionRepository = mock(RedisSessionRepository.class);
        sessionService = new SessionService(redisSessionRepository);
    }

    @Test
    @DisplayName("Сохранение сессии: вызов метода репозитория с правильными параметрами")
    void saveSession_shouldCallRepository() {
        String username = "user";
        String token = "token";
        Duration duration = Duration.ofMinutes(30);

        sessionService.saveSession(username, token, duration);

        verify(redisSessionRepository).saveSession(username, token, duration);
    }

    @Test
    @DisplayName("Проверка сессии: возвращает true, если сессия существует и не истекла")
    void isSessionValid_shouldReturnTrue_whenSessionExistsAndNotExpired() {
        String username = "user";
        String token = "token";

        when(redisSessionRepository.isSessionExists(username, token)).thenReturn(true);
        when(redisSessionRepository.isSessionExpired(username, token)).thenReturn(false);

        assertTrue(sessionService.isSessionValid(username, token));
    }

    @Test
    @DisplayName("Проверка сессии: возвращает false, если сессия не существует")
    void isSessionValid_shouldReturnFalse_whenSessionNotExists() {
        String username = "user";
        String token = "token";

        when(redisSessionRepository.isSessionExists(username, token)).thenReturn(false);

        assertFalse(sessionService.isSessionValid(username, token));
    }

    @Test
    @DisplayName("Проверка сессии: возвращает false, если сессия истекла")
    void isSessionValid_shouldReturnFalse_whenSessionExpired() {
        String username = "user";
        String token = "token";

        when(redisSessionRepository.isSessionExists(username, token)).thenReturn(true);
        when(redisSessionRepository.isSessionExpired(username, token)).thenReturn(true);

        assertFalse(sessionService.isSessionValid(username, token));
    }

    @Test
    @DisplayName("Обновление сессии: вызов метода репозитория с правильными параметрами")
    void updateSession_shouldCallRepository() {
        String username = "user";
        String token = "token";
        Duration duration = Duration.ofMinutes(20);

        sessionService.updateSession(username, token, duration);

        verify(redisSessionRepository).updateSession(username, token, duration);
    }

    @Test
    @DisplayName("Удаление сессии: вызов метода репозитория с правильным именем пользователя")
    void removeSession_shouldCallRepository() {
        String username = "user";

        sessionService.removeSession(username);

        verify(redisSessionRepository).removeSession(username);
    }
}
