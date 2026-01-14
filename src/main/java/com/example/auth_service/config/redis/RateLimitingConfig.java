package com.example.auth_service.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Конфигурация Redis для механизма ограничения частоты запросов (rate limiting).
 *
 * Определяет специализированный {@link RedisTemplate}, используемый инфраструктурными
 * компонентами приложения (фильтры, аспекты) для учёта количества вызовов
 * и контроля нагрузки на сервис.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Создаёт {@link RedisTemplate} для хранения счётчиков запросов.
     * <p>
     * Настраивает сериализацию ключей и значений в строки, включая хэш-ключи,
     * для корректного использования в механизме rate limiting.
     *
     * @param connectionFactory фабрика подключений к Redis; должна быть корректно инициализирована
     * @return {@link RedisTemplate} для операций учёта частоты запросов
     */
    @Bean
    public RedisTemplate<String, Long> rateLimitRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
