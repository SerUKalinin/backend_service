package com.example.auth_service.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация ModelMapper для приложения аутентификации.
 * <p>
 * Предоставляет бин {@link ModelMapper} для маппинга данных между DTO и сущностями
 * на сервисном слое, обеспечивая автоматическое копирование свойств и преобразование объектов.
 * Используется в сервисах и контроллерах для упрощения преобразований и поддержания чистоты кода.
 * </p>
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Создаёт и возвращает бин {@link ModelMapper}.
     * <p>
     * Рекомендуется использовать для преобразования объектов одного типа в другой,
     * например, сущности в DTO или DTO в сущность, чтобы уменьшить количество ручного маппинга.
     * </p>
     *
     * @return готовый экземпляр {@link ModelMapper}, готовый к внедрению в сервисные слои
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
