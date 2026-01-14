package com.example.auth_service.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация кэширования для приложения аутентификации.
 * <p>
 * Включает поддержку кэширования Spring и настраивает in-memory {@link CacheManager},
 * который используется для ускорения доступа к часто запрашиваемым данным на сервисном слое.
 * </p>
 * <p>
 * Основная цель — уменьшение количества обращений к базе данных и повышение производительности
 * при работе с пользовательскими данными и другими часто используемыми сущностями.
 * </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Создаёт и возвращает {@link CacheManager} для управления кэшами приложения.
     * <p>
     * Настроен на использование ConcurrentMap в памяти для хранения кэшированных данных.
     * Определяет кэши {@code userDetails} и {@code userInfo} для хранения информации о пользователях.
     * </p>
     *
     * @return экземпляр {@link CacheManager}, готовый к использованию в сервисном слое для кэширования данных
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList("userDetails", "userInfo"));
        return cacheManager;
    }
}
