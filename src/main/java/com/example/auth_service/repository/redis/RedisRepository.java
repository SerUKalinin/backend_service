package com.example.auth_service.repository.redis;

import java.util.Date;

/**
 * Интерфейс репозитория для работы с Redis.
 *
 * <p>Предоставляет абстракцию для операций с Redis, включая сохранение значений с
 * временем истечения, проверку существования ключей, получение значений и управление
 * временем жизни сессий. Используется для реализации черного списка JWT и управления
 * сессиями пользователей.</p>
 */
public interface RedisRepository {

    /**
     * Сохраняет значение в Redis с указанным ключом и временем истечения.
     *
     * @param key   Ключ, по которому сохраняется значение. Не может быть пустым или null.
     * @param value Значение для сохранения. Не может быть null.
     * @param date  Время, когда ключ должен истечь и быть удален. Не может быть null.
     * @throws IllegalArgumentException Если ключ или значение пустые, либо дата null.
     */
    void save(String key, String value, Date date);

    /**
     * Устанавливает время истечения для существующего ключа в Redis.
     *
     * @param key  Ключ, для которого устанавливается время истечения. Не может быть пустым или null.
     * @param date Время, когда ключ должен истечь. Не может быть null.
     * @throws IllegalArgumentException Если ключ пустой или дата null.
     */
    void expireAt(String key, Date date);

    /**
     * Проверяет существование ключа в Redis.
     *
     * @param key Ключ для проверки. Не может быть пустым или null.
     * @return true, если ключ существует; false, если ключ отсутствует.
     * @throws IllegalArgumentException Если ключ пустой или null.
     */
    boolean isExists(String key);

    /**
     * Получает значение, связанное с указанным ключом.
     *
     * @param key Ключ, по которому извлекается значение. Не может быть пустым или null.
     * @return Значение, связанное с ключом, или null, если ключ не найден.
     * @throws IllegalArgumentException Если ключ пустой или null.
     */
    String getValue(String key);

    /**
     * Получает время истечения сессии пользователя.
     *
     * @param username Имя пользователя. Не может быть null.
     * @return Время истечения сессии, или null, если сессия не найдена.
     */
    Date getSessionExpiry(String username);

    /**
     * Обновляет время жизни сессии пользователя в Redis.
     *
     * @param username Имя пользователя. Не может быть null.
     * @param duration Новая длительность сессии. Не может быть null.
     */
    void refreshSession(String username, java.time.Duration duration);
}
