package com.example.auth_service.repository.redis;

import java.util.Date;

/**
 * Интерфейс репозитория для работы с Redis.
 * Предоставляет методы для сохранения значений в Redis, их извлечения, проверки существования ключей и управления временем истечения.
 */
public interface RedisRepository {

    /**
     * Сохраняет значение в Redis с указанным ключом и временем истечения.
     *
     * @param key   Ключ, по которому сохраняется значение в Redis.
     * @param value Значение, которое будет сохранено.
     * @param date  Время, когда ключ должен истечь и быть удален из Redis.
     * @throws IllegalArgumentException Если ключ или значение пустое.
     */
    void save(String key, String value, Date date);

    /**
     * Устанавливает время истечения для ключа в Redis.
     *
     * @param key  Ключ, для которого будет установлено время истечения.
     * @param date Время, когда ключ должен истечь.
     * @throws IllegalArgumentException Если ключ или дата истечения пустые.
     */
    void expireAt(String key, Date date);

    /**
     * Проверяет, существует ли ключ в Redis.
     *
     * @param key Ключ, который нужно проверить.
     * @return true, если ключ существует; false, если нет.
     * @throws IllegalArgumentException Если ключ пустой.
     */
    boolean isExists(String key);

    /**
     * Получает значение, связанное с ключом в Redis.
     *
     * @param key Ключ, по которому нужно получить значение.
     * @return Значение, связанное с ключом, или null, если ключ не найден.
     * @throws IllegalArgumentException Если ключ пустой.
     */
    String getValue(String key);

    /**
     * Получает время истечения сессии.
     *
     * @param username Имя пользователя.
     * @return Время истечения сессии или null, если сессия не найдена.
     */
    Date getSessionExpiry(String username);

    /**
     * Обновляет время жизни сессии.
     *
     * @param username Имя пользователя.
     * @param duration Новая длительность сессии.
     */
    void refreshSession(String username, java.time.Duration duration);
}
