package com.example.auth_service.config.redis;

import com.example.auth_service.exception.RedisConfigurationException;
import com.example.auth_service.model.PasswordResetToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Основная конфигурация Redis для приложения.
 *
 * <p>Отвечает за:</p>
 * <ul>
 *     <li>инициализацию соединения с Redis (Lettuce);</li>
 *     <li>регистрацию специализированных {@link RedisTemplate};</li>
 *     <li>поддержку Redis-репозиториев.</li>
 * </ul>
 *
 * <p>Конфигурация предназначена для хранения сессий, токенов сброса пароля
 * и других инфраструктурных данных.</p>
 */
@Slf4j
@Configuration
@EnableRedisRepositories(basePackages = "com.example.auth_service.repository.redis")
public class RedisConfig {

    @Value("${spring.redis.data.host}")
    private String host;

    @Value("${spring.redis.data.port}")
    private int port;

    @Value("${spring.redis.data.password}")
    private String password;

    /**
     * Создаёт фабрику подключений к Redis на базе Lettuce.
     *
     * @return {@link LettuceConnectionFactory} для Redis
     * @throws RedisConfigurationException если параметры подключения некорректны
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        if (host == null || host.isBlank()) {
            throw new RedisConfigurationException("Хост Redis не может быть пустым");
        }
        if (port <= 0 || port > 65535) {
            throw new RedisConfigurationException("Порт Redis должен быть в диапазоне 1–65535");
        }
        if (password == null || password.isBlank()) {
            throw new RedisConfigurationException("Пароль Redis не может быть пустым");
        }

        log.info("Инициализация подключения к Redis: host={}, port={}", host, port);

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setPassword(password);

        return new LettuceConnectionFactory(configuration);
    }

    /**
     * RedisTemplate для работы со строковыми ключами и значениями.
     *
     * <p>Используется для простых инфраструктурных операций
     * (хранение идентификаторов, флагов, временных значений).</p>
     *
     * @param connectionFactory фабрика подключений Redis
     * @return {@link RedisTemplate} со строковой сериализацией
     */
    @Bean
    public RedisTemplate<String, String> stringRedisTemplateCustom(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * RedisTemplate для хранения токенов сброса пароля.
     *
     * <p>Объекты {@link PasswordResetToken} сериализуются в JSON
     * с использованием общего {@link ObjectMapper} приложения.</p>
     *
     * @param connectionFactory фабрика подключений Redis
     * @param objectMapper ObjectMapper приложения
     * @return {@link RedisTemplate} для токенов сброса пароля
     */
    @Bean
    public RedisTemplate<String, PasswordResetToken> passwordResetTokenRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, PasswordResetToken> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        template.afterPropertiesSet();
        return template;
    }

}
