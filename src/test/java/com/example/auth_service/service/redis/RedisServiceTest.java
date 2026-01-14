package com.example.auth_service.service.redis;

import com.example.auth_service.model.PasswordResetToken;
import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.redis.RedisPasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisServiceTest {

    RedisTemplate<String, String> redisTemplate;
    ValueOperations<String, String> valueOps;
    RedisPasswordResetTokenRepository passwordResetRepo;
    RefreshTokenRepository refreshTokenRepo;
    RedisService redisService;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        passwordResetRepo = mock(RedisPasswordResetTokenRepository.class);
        refreshTokenRepo = mock(RefreshTokenRepository.class);

        redisService = new RedisService(redisTemplate, passwordResetRepo, refreshTokenRepo);
    }

    @Test
    @DisplayName("Сохранение кода подтверждения в Redis")
    void saveConfirmationCode_shouldSaveInRedis() {
        redisService.saveConfirmationCode("email@test.com", "123456");
        verify(valueOps).set("email@test.com", "123456", 10, java.util.concurrent.TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Проверка кода подтверждения: совпадение")
    void checkConfirmationCode_shouldReturnTrueIfMatch() {
        when(valueOps.get("email@test.com")).thenReturn("123456");
        assertTrue(redisService.checkConfirmationCode("email@test.com", "123456"));
    }

    @Test
    @DisplayName("Проверка кода подтверждения: несовпадение")
    void checkConfirmationCode_shouldReturnFalseIfMismatch() {
        when(valueOps.get("email@test.com")).thenReturn("123456");
        assertFalse(redisService.checkConfirmationCode("email@test.com", "000000"));
    }

    @Test
    @DisplayName("Удаление кода подтверждения")
    void deleteConfirmationCode_shouldDeleteKey() {
        redisService.deleteConfirmationCode("email@test.com");
        verify(redisTemplate).delete("email@test.com");
    }

    @Test
    @DisplayName("Сохранение токена сброса пароля и установка времени жизни")
    void savePasswordResetToken_shouldSaveAndSetExpire() {
        redisService.savePasswordResetToken("email@test.com", "token123", Duration.ofMinutes(15));
        verify(passwordResetRepo).save(any(PasswordResetToken.class));
        verify(redisTemplate).expire("password_reset:email@test.com", Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("Проверка токена сброса пароля: совпадение")
    void checkPasswordResetToken_shouldReturnTrueIfMatch() {
        PasswordResetToken token = new PasswordResetToken("password_reset:email@test.com", "token123");
        when(passwordResetRepo.findById("password_reset:email@test.com")).thenReturn(Optional.of(token));
        assertTrue(redisService.checkPasswordResetToken("email@test.com", "token123"));
    }

    @Test
    @DisplayName("Проверка токена сброса пароля: несовпадение")
    void checkPasswordResetToken_shouldReturnFalseIfMismatch() {
        PasswordResetToken token = new PasswordResetToken("password_reset:email@test.com", "token123");
        when(passwordResetRepo.findById("password_reset:email@test.com")).thenReturn(Optional.of(token));
        assertFalse(redisService.checkPasswordResetToken("email@test.com", "wrongToken"));
    }

    @Test
    @DisplayName("Удаление токена сброса пароля")
    void deletePasswordResetToken_shouldDeleteFromRepo() {
        redisService.deletePasswordResetToken("email@test.com");
        verify(passwordResetRepo).deleteById("password_reset:email@test.com");
    }

    @Test
    @DisplayName("Сохранение refresh-токена в Redis и БД")
    void saveRefreshToken_shouldSaveInRedisAndDB() {
        redisService.saveRefreshToken("user1", "refresh123", Duration.ofHours(1));
        verify(valueOps).set(startsWith("refresh:user1:"), eq("user1"), anyLong(), any());
        verify(refreshTokenRepo).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Проверка refresh-токена: найден в Redis")
    void isRefreshTokenValid_shouldReturnTrueIfInRedis() {
        when(redisTemplate.hasKey("refresh:user1:refresh123")).thenReturn(true);
        assertTrue(redisService.isRefreshTokenValid("user1", "refresh123"));
    }

    @Test
    @DisplayName("Проверка refresh-токена: найден в БД, отсутствует в Redis")
    void isRefreshTokenValid_shouldReturnTrueIfInDBButNotRedis() {
        when(redisTemplate.hasKey("refresh:user1:refresh123")).thenReturn(false);
        RefreshToken token = RefreshToken.builder()
                .username("user1")
                .token("refresh123")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        when(refreshTokenRepo.findByToken("refresh123")).thenReturn(Optional.of(token));

        assertTrue(redisService.isRefreshTokenValid("user1", "refresh123"));
        verify(valueOps).set(startsWith("refresh:user1:"), eq("user1"), anyLong(), any());
    }

    @Test
    @DisplayName("Проверка refresh-токена: не найден или истёк")
    void isRefreshTokenValid_shouldReturnFalseIfExpiredOrNotFound() {
        when(redisTemplate.hasKey("refresh:user1:refresh123")).thenReturn(false);
        when(refreshTokenRepo.findByToken("refresh123")).thenReturn(Optional.empty());
        assertFalse(redisService.isRefreshTokenValid("user1", "refresh123"));
    }

    @Test
    @DisplayName("Удаление refresh-токена из Redis и БД")
    void deleteRefreshToken_shouldDeleteFromRedisAndDB() {
        redisService.deleteRefreshToken("user1", "refresh123");
        verify(redisTemplate).delete("refresh:user1:refresh123");
        verify(refreshTokenRepo).deleteByToken("refresh123");
    }

    @Test
    @DisplayName("Получение username по refresh-токену из Redis")
    void findUsernameByRefreshToken_shouldReturnUsernameFromRedis() {
        Set<String> keys = Set.of("refresh:user1:refresh123");
        when(redisTemplate.keys("refresh:*:refresh123")).thenReturn(keys);
        assertEquals("user1", redisService.findUsernameByRefreshToken("refresh123"));
    }

    @Test
    @DisplayName("Получение username по refresh-токену из БД, если Redis пуст")
    void findUsernameByRefreshToken_shouldReturnUsernameFromDBIfRedisEmpty() {
        when(redisTemplate.keys("refresh:*:refresh123")).thenReturn(Set.of());
        RefreshToken token = RefreshToken.builder().username("user1").token("refresh123").build();
        when(refreshTokenRepo.findByToken("refresh123")).thenReturn(Optional.of(token));
        assertEquals("user1", redisService.findUsernameByRefreshToken("refresh123"));
    }

    @Test
    @DisplayName("Получение username по refresh-токену: не найден")
    void findUsernameByRefreshToken_shouldReturnNullIfNotFound() {
        when(redisTemplate.keys("refresh:*:refresh123")).thenReturn(Set.of());
        when(refreshTokenRepo.findByToken("refresh123")).thenReturn(Optional.empty());
        assertNull(redisService.findUsernameByRefreshToken("refresh123"));
    }
}
