package com.example.auth_service.service.redis;

import com.example.auth_service.repository.redis.RedisPasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import com.example.auth_service.model.PasswordResetToken;
import java.util.Set;
import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Сервис для управления данными в Redis.
 *
 * <p>Отвечает за хранение и проверку кодов подтверждения, токенов сброса пароля и refresh-токенов.
 * Интегрируется с базой данных для долговременного хранения refresh-токенов.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    /**
     * Компонент Spring для работы с Redis.
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Репозиторий для хранения токенов сброса пароля в Redis.
     */
    private final RedisPasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Репозиторий для хранения refresh-токенов в БД.
     */
    private final RefreshTokenRepository refreshTokenRepository;

    private static final String PASSWORD_RESET_TOKEN_PREFIX = "password_reset:";

    /**
     * Сохраняет код подтверждения для email пользователя в Redis.
     * Код истекает через 10 минут.
     *
     * @param email Email пользователя.
     * @param code  Код подтверждения.
     */
    public void saveConfirmationCode(String email, String code) {
        log.info("Сохраняем код подтверждения для email: {}", email);
        redisTemplate.opsForValue().set(email, code, 10, TimeUnit.MINUTES);
        log.info("Код подтверждения для email: {} успешно сохранен с истечением через 10 минут", email);
    }

    /**
     * Проверяет соответствие введенного кода подтверждения сохраненному в Redis.
     *
     * @param email Email пользователя.
     * @param code  Введенный код подтверждения.
     * @return true, если код совпадает с сохраненным.
     */
    public boolean checkConfirmationCode(String email, String code) {
        log.info("Проверка кода подтверждения для email: {}", email);
        String storedCode = redisTemplate.opsForValue().get(email);
        boolean isCodeValid = storedCode != null && storedCode.equals(code);
        if (isCodeValid) {
            log.info("Код подтверждения для email: {} совпадает", email);
        } else {
            log.warn("Код подтверждения для email: {} не совпадает", email);
        }
        return isCodeValid;
    }

    /**
     * Удаляет код подтверждения из Redis.
     *
     * @param email Email пользователя.
     */
    public void deleteConfirmationCode(String email) {
        log.info("Удаляем код подтверждения для email: {}", email);
        redisTemplate.delete(email);
        log.info("Код подтверждения для email: {} успешно удален", email);
    }

    /**
     * Сохраняет токен для сброса пароля в Redis.
     *
     * @param email    Email пользователя.
     * @param token    Токен для сброса пароля.
     * @param duration Время жизни токена.
     */
    public void savePasswordResetToken(String email, String token, Duration duration) {
        String key = PASSWORD_RESET_TOKEN_PREFIX + email;
        PasswordResetToken resetToken = new PasswordResetToken(key, token);
        passwordResetTokenRepository.save(resetToken);
        redisTemplate.expire(key, duration);
    }

    /**
     * Проверяет валидность токена для сброса пароля.
     *
     * @param email Email пользователя.
     * @param token Токен для проверки.
     * @return true, если токен действителен.
     */
    public boolean checkPasswordResetToken(String email, String token) {
        String key = PASSWORD_RESET_TOKEN_PREFIX + email;
        return passwordResetTokenRepository.findById(key)
                .map(resetToken -> token.equals(resetToken.getToken()))
                .orElse(false);
    }

    /**
     * Удаляет токен для сброса пароля из Redis.
     *
     * @param email Email пользователя.
     */
    public void deletePasswordResetToken(String email) {
        String key = PASSWORD_RESET_TOKEN_PREFIX + email;
        passwordResetTokenRepository.deleteById(key);
    }

    /**
     * Сохраняет refresh-токен пользователя в Redis и БД.
     *
     * @param username     Имя пользователя.
     * @param refreshToken Токен обновления.
     * @param duration     Время жизни токена.
     */
    public void saveRefreshToken(String username, String refreshToken, Duration duration) {
        String key = "refresh:" + username + ":" + refreshToken;
        redisTemplate.opsForValue().set(key, username, duration.getSeconds(), TimeUnit.SECONDS);

        RefreshToken entity = RefreshToken.builder()
                .username(username)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(duration.getSeconds()))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(entity);
    }

    /**
     * Проверяет валидность refresh-токена для пользователя.
     *
     * @param username     Имя пользователя.
     * @param refreshToken Токен обновления.
     * @return true, если токен действителен.
     */
    public boolean isRefreshTokenValid(String username, String refreshToken) {
        String key = "refresh:" + username + ":" + refreshToken;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return true;
        }
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);
        if (tokenOpt.isPresent() && tokenOpt.get().getUsername().equals(username)
                && tokenOpt.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            Duration duration = Duration.between(LocalDateTime.now(), tokenOpt.get().getExpiresAt());
            redisTemplate.opsForValue().set(key, username, duration.getSeconds(), TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * Удаляет refresh-токен пользователя из Redis и БД.
     *
     * @param username     Имя пользователя.
     * @param refreshToken Токен обновления.
     */
    public void deleteRefreshToken(String username, String refreshToken) {
        String key = "refresh:" + username + ":" + refreshToken;
        redisTemplate.delete(key);
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    /**
     * Находит имя пользователя по refresh-токену.
     *
     * @param refreshToken Refresh-токен.
     * @return Имя пользователя или null, если токен не найден.
     */
    public String findUsernameByRefreshToken(String refreshToken) {
        String pattern = "refresh:*:" + refreshToken;
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            String key = keys.iterator().next();
            String[] parts = key.split(":");
            if (parts.length >= 3) {
                return parts[1];
            }
        }
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);
        return tokenOpt.map(RefreshToken::getUsername).orElse(null);
    }
}
