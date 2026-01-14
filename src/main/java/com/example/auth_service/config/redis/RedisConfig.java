package com.example.auth_service.config.redis;

import com.example.auth_service.exception.RedisConfigurationException;
import com.example.auth_service.model.PasswordResetToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Конфигурация Redis для приложения.
 * <p>
 * Отвечает за создание соединения с Redis и регистрацию {@link RedisTemplate},
 * используемых для хранения сессий, токенов сброса пароля и кэша.
 * В качестве клиента используется Lettuce.
 * </p>
 */
@Slf4j
@Configuration
@EnableRedisRepositories(basePackages = "com.example.auth_service.repository.redis")
public class RedisConfig {

    /**
     * Хост Redis-сервера.
     * <p>
     * Загружается из конфигурации приложения через {@code spring.redis.data.host}.
     * Не может быть пустым или null.
     */
    @Value("${spring.redis.data.host}")
    private String host;

    /**
     * Порт Redis-сервера.
     * <p>
     * Загружается из конфигурации приложения через {@code spring.redis.data.port}.
     * Должен быть в диапазоне 1–65535.
     */
    @Value("${spring.redis.data.port}")
    private int port;

    /**
     * Пароль Redis-сервера.
     * <p>
     * Загружается из конфигурации приложения через {@code spring.redis.data.password}.
     * Не может быть null.
     */
    @Value("${spring.redis.data.password}")
    private String password;

    /**
     * Создаёт {@link LettuceConnectionFactory} для подключения к Redis.
     * <p>
     * Выполняет базовую валидацию параметров подключения и используется
     * всеми {@link RedisTemplate} в приложении.
     *
     * @return фабрика подключений к Redis
     * @throws RedisConfigurationException если параметры подключения некорректны
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
     * Создаёт {@link RedisTemplate} для работы со строковыми ключами и значениями.
     * <p>
     * Используется для простых операций хранения данных без сериализации объектов.
     *
     * @param connectionFactory фабрика подключений Redis; не может быть null
     * @return {@link RedisTemplate} для работы с {@code String}-ключами и значениями
     * @throws IllegalArgumentException если {@code connectionFactory} null
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

    /**
     * Создаёт {@link RedisTemplate} для хранения токенов сброса пароля.
     * <p>
     * Сериализует объекты {@link PasswordResetToken} в JSON для хранения в Redis.
     *
     * @param connectionFactory фабрика подключений Redis; не может быть null
     * @return {@link RedisTemplate} для работы с токенами сброса пароля
     */
    @Bean
    public RedisTemplate<String, PasswordResetToken> passwordResetTokenRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, PasswordResetToken> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(PasswordResetToken.class));
        return template;
    }
}
