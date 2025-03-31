package com.example.auth_service.config;

import com.example.auth_service.exception.InvalidCorsConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация CORS (Cross-Origin Resource Sharing) для сервиса аутентификации.
 * Позволяет управлять разрешёнными источниками, методами и заголовками для кросс-доменных запросов.
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Настраивает разрешённые CORS-правила для веб-приложения.
     *
     * @param registry Реестр CORS-настроек.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String allowedOrigin = "http://localhost:63342"; // Фронтенд работает на порту 63342

        if (allowedOrigin.isBlank()) {
            log.error("Ошибка конфигурации CORS: список разрешённых источников пуст");
            throw new InvalidCorsConfigurationException("Список разрешённых источников CORS не может быть пустым");
        }

        log.info("Настройка CORS: разрешённый источник - {}", allowedOrigin);

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept")
                .allowCredentials(true);

        log.info("CORS конфигурация успешно применена");
    }
}
