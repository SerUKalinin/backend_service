package com.example.auth_service.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Конфигурация для rate limiting.
 * Настраивает RedisTemplate для работы с ограничением частоты запросов.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Создает и настраивает RedisTemplate для rate limiting.
     * Использует StringRedisSerializer для сериализации ключей и значений.
     *
     * @param connectionFactory Фабрика подключений к Redis
     * @return Настроенный RedisTemplate
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