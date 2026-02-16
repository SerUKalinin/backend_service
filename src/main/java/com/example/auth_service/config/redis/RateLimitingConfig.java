package com.example.auth_service.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Конфигурация Redis для механизма ограничения частоты запросов (rate limiting).
 *
 * <p>Определяет специализированный {@link RedisTemplate}, предназначенный
 * исключительно для инфраструктурных задач, связанных с ограничением частоты вызовов
 * (аспекты, фильтры защиты API).</p>
 *
 * <p>Данный шаблон не предназначен для хранения бизнес-данных
 * или использования в сервисном слое.</p>
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Создаёт {@link RedisTemplate} для хранения счётчиков запросов.
     *
     * <p>Ключи сериализуются в строки, значения — в числовой формат {@link Long},
     * что обеспечивает корректную и безопасную работу операций инкремента
     * в Redis.</p>
     *
     * @param connectionFactory фабрика подключений к Redis
     * @return {@link RedisTemplate} для учёта частоты запросов
     */
    @Bean
    public RedisTemplate<String, Long> rateLimitRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Long.class));

        template.afterPropertiesSet();
        return template;
    }
}
