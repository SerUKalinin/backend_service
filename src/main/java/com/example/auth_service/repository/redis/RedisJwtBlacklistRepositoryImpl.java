package com.example.auth_service.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Реализация репозитория для работы с черным списком JWT в Redis.
 * Этот класс управляет сохранением и удалением JWT-токенов в Redis, а также проверяет их наличие.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisJwtBlacklistRepositoryImpl implements RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SESSION_PREFIX = "session:";

    /**
     * Сохраняет значение в Redis с указанным ключом и временем истечения.
     *
     * @param key   Ключ, по которому сохраняется значение в Redis.
     * @param value Значение, которое будет сохранено.
     * @param date  Время, когда ключ должен истечь и быть удален из Redis.
     * @throws IllegalArgumentException Если ключ или значение пустое.
     */
    @Override
    public void save(String key, String value, Date date) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Ключ не может быть пустым.");
        }

        // Логирование сохранения
        log.info("Сохраняем значение в Redis: ключ = {}, значение = {}", key, value);

        this.redisTemplate.opsForValue().set(key, value);
        expireAt(key, date);
    }

    /**
     * Устанавливает время истечения для ключа в Redis.
     *
     * @param key  Ключ, для которого будет установлено время истечения.
     * @param date Время, когда ключ должен истечь.
     * @throws IllegalArgumentException Если ключ или время истечения не указаны.
     */
    @Override
    public void expireAt(String key, Date date) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Ключ не может быть пустым.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Дата истечения не может быть пустой.");
        }

        // Логирование установки времени истечения
        log.info("Устанавливаем время истечения для ключа = {} на дату = {}", key, date);

        redisTemplate.expireAt(key, date);
    }

    /**
     * Проверяет, существует ли ключ в Redis.
     *
     * @param key Ключ, который нужно проверить.
     * @return true, если ключ существует; false, если нет.
     * @throws IllegalArgumentException Если ключ пустой.
     */
    @Override
    public boolean isExists(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Ключ не может быть пустым.");
        }

        // Логирование проверки существования ключа
        log.info("Проверка существования ключа в Redis: ключ = {}", key);

        return this.redisTemplate.hasKey(key);
    }

    /**
     * Получает значение, связанное с ключом в Redis.
     *
     * @param key Ключ, по которому нужно получить значение.
     * @return Значение, связанное с ключом, или null, если ключ не найден.
     * @throws IllegalArgumentException Если ключ пустой.
     */
    @Override
    public String getValue(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Ключ не может быть пустым.");
        }

        // Логирование получения значения
        log.info("Получаем значение для ключа из Redis: ключ = {}", key);

        return this.redisTemplate.opsForValue().get(key);
    }

    /**
     * Получает время истечения сессии.
     *
     * @param username Имя пользователя.
     * @return Время истечения сессии или null, если сессия не найдена.
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
     * Обновляет время жизни сессии.
     *
     * @param username Имя пользователя.
     * @param duration Новая длительность сессии.
     */
    @Override
    public void refreshSession(String username, java.time.Duration duration) {
        String key = SESSION_PREFIX + username;
        if (redisTemplate.hasKey(key)) {
            String token = (String) redisTemplate.opsForHash().get(key, "token");
            if (token != null) {
                redisTemplate.opsForHash().put(key, "expiry", String.valueOf(System.currentTimeMillis() + duration.toMillis()));
                redisTemplate.expire(key, duration);
                log.info("Сессия пользователя {} обновлена на {}", username, duration);
            }
        }
    }
}
