package com.example.auth_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:63342") // Фронтенд работает на порту 63342
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Добавлены дополнительные методы
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept") // Указаны часто используемые заголовки
                .allowCredentials(true); // Разрешаем использование cookies
    }
}
