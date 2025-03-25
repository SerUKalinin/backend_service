package com.example.auth_service.config.redis;

import com.example.auth_service.exception.RedisConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Конфигурация Redis для приложения.
 * Использует Lettuce в качестве клиента для взаимодействия с сервером Redis.
 */
@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.redis.data.host}")
    private String host;

    @Value("${spring.redis.data.port}")
    private int port;

    @Value("${spring.redis.data.password}")
    private String password;

    /**
     * Создает и настраивает фабрику подключений к Redis.
     *
     * @return {@link LettuceConnectionFactory} для работы с Redis.
     * @throws RedisConfigurationException если конфигурация некорректна.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        if (host == null || host.isBlank()) {
            log.error("Хост Redis не может быть пустым");
            throw new RedisConfigurationException("Хост Redis не может быть пустым");
        }
        if (port <= 0 || port > 65535) {
            log.error("Порт Redis должен быть в диапазоне 1-65535");
            throw new RedisConfigurationException("Порт Redis должен быть в диапазоне 1-65535");
        }
        if (password == null) {
            log.error("Пароль Redis не может быть null");
            throw new RedisConfigurationException("Пароль Redis не может быть null");
        }

        log.info("Настройка соединения с Redis: хост={}, порт={}", host, port);

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        redisConfig.setPassword(password);

        return new LettuceConnectionFactory(redisConfig);
    }

    /**
     * Создает и настраивает шаблон Redis для работы с ключами и значениями типа String.
     *
     * @param connectionFactory Фабрика подключений к Redis.
     * @return {@link RedisTemplate} для работы с Redis.
     * @throws IllegalArgumentException если передана некорректная фабрика подключений.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            log.error("Фабрика подключений Redis не может быть null");
            throw new IllegalArgumentException("Фабрика подключений Redis не может быть null");
        }

        log.info("Создание RedisTemplate с переданной фабрикой подключений");

        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
