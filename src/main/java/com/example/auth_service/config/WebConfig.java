package com.example.auth_service.config;

import com.example.auth_service.exception.InvalidCorsConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация CORS для приложения аутентификации.
 * <p>
 * Настраивает кросс-доменные HTTP-запросы, определяя разрешённые источники, методы и заголовки.
 * Используется на уровне веб-конфигурации Spring MVC и применяется ко всем REST-эндпоинтам приложения.
 * </p>
 * <p>
 * Контролирует корректность настроек и предотвращает пустой список разрешённых источников.
 * </p>
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Настраивает правила CORS для всех эндпоинтов приложения.
     * <p>
     * Метод задаёт:
     * <ul>
     *     <li>Список разрешённых источников</li>
     *     <li>Допустимые HTTP методы</li>
     *     <li>Разрешённые заголовки</li>
     *     <li>Поддержку передачи учетных данных</li>
     * </ul>
     * Также проверяет, что список источников не пуст, и логирует процесс настройки.
     * </p>
     *
     * @param registry реестр CORS-настроек Spring MVC; не может быть null
     * @throws InvalidCorsConfigurationException если список разрешённых источников пуст
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = {
                "http://localhost:5173",
                "http://localhost:63342",
                "http://localhost:3000",
        };

        if (allowedOrigins.length == 0) {
            log.error("Ошибка конфигурации CORS: список разрешённых источников пуст");
            throw new InvalidCorsConfigurationException("Список разрешённых источников CORS не может быть пустым");
        }

        log.info("Настройка CORS: разрешённые источники - {}", (Object) allowedOrigins);

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
                .allowCredentials(true);

        log.info("CORS конфигурация успешно применена");
    }
}
