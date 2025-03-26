package com.example.auth_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Разрешаем доступ с фронтенда (порт 63342) на бэкенд (порт 8080)
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:63342") // Фронтенд будет работать на порту 63342
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Разрешаем методы
                .allowedHeaders("*") // Разрешаем все заголовки
                .allowCredentials(true); // Разрешаем использование cookies, если нужно
    }
}
