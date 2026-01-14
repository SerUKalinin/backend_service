package com.example.auth_service.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Репозиторий для работы с черным списком JWT и управлением сессиями в Redis.
 *
 * <p>Обеспечивает сохранение, проверку, получение и обновление JWT-токенов,
 * а также управление временем жизни пользовательских сессий. Используется
 * для реализации механизма блокировки токенов и контроля активности сессий.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisJwtBlacklistRepositoryImpl implements RedisRepository {

    /** RedisTemplate для работы с ключами и значениями в Redis. */
    private final RedisTemplate<String, String> redisTemplate;

    /** Префикс ключей для хранения сессий пользователей. */
    private static final String SESSION_PREFIX = "session:";

    /**
     * Сохраняет значение в Redis с указанным ключом и временем истечения.
     *
     * @param key   Ключ для сохранения значения. Не может быть пустым.
     * @param value Значение для сохранения. Не может быть пустым.
     * @param date  Дата и время истечения ключа. Не может быть null.
     * @throws IllegalArgumentException если ключ или значение пустое, или дата null.
     */
    @Override
    public void save(String key, String value, Date date) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Ключ не может быть пустым.");
        }

        this.redisTemplate.opsForValue().set(key, value);
        expireAt(key, date);
    }

    /**
     * Устанавливает время истечения для указанного ключа в Redis.
     *
     * @param key  Ключ, для которого устанавливается истечение. Не может быть пустым.
     * @param date Дата и время истечения. Не может быть null.
     * @throws IllegalArgumentException если ключ пустой или дата null.
     */
    @Override
    public void expireAt(String key, Date date) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Ключ не может быть пустым.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Дата истечения не может быть пустой.");
        }

        redisTemplate.expireAt(key, date);
    }

    /**
     * Проверяет наличие ключа в Redis.
     *
     * @param key Ключ для проверки. Не может быть пустым.
     * @return true, если ключ существует; false — если ключ отсутствует.
     * @throws IllegalArgumentException если ключ пустой.
     */
    @Override
    public boolean isExists(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Ключ не может быть пустым.");
        }

        return this.redisTemplate.hasKey(key);
    }

    /**
     * Получает значение из Redis по ключу.
     *
     * @param key Ключ для получения значения. Не может быть пустым.
     * @return Значение, связанное с ключом, или null, если ключ отсутствует.
     * @throws IllegalArgumentException если ключ пустой.
     */
    @Override
    public String getValue(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Ключ не может быть пустым.");
        }

        return this.redisTemplate.opsForValue().get(key);
    }

    /**
     * Получает время истечения сессии пользователя.
     *
     * @param username Имя пользователя. Не может быть null или пустым.
     * @return Время истечения сессии, либо null, если сессия отсутствует.
     */
    @Override
    public Date getSessionExpiry(String username) {
        String key = SESSION_PREFIX + username;
        if (redisTemplate.hasKey(key)) {
            String expiryStr = (String) redisTemplate.opsForHash().get(key, "expiry");
            if (expiryStr != null) {
                return new Date(Long.parseLong(expiryStr));
            }
        }
        return null;
    }

    /**
     * Обновляет время жизни сессии пользователя.
     *
     * @param username Имя пользователя. Не может быть null или пустым.
     * @param duration Новая длительность сессии. Не может быть null.
     */
    @Override
    public void refreshSession(String username, java.time.Duration duration) {
        String key = SESSION_PREFIX + username;
        if (redisTemplate.hasKey(key)) {
            String token = (String) redisTemplate.opsForHash().get(key, "token");
            if (token != null) {
                redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));
                redisTemplate.expire(key, duration);
                log.debug("Сессия пользователя {} обновлена на {}", username, duration);
            }
        }
    }
}
