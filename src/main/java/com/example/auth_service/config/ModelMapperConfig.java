package com.example.auth_service.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для создания и настройки {@link ModelMapper}.
 * <p>
 * Этот класс предоставляет бины для преобразования объектов с помощью библиотеки ModelMapper,
 * которая помогает в маппинге данных между различными объектами (например, DTO и сущности).
 * </p>
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Создаёт и возвращает экземпляр {@link ModelMapper}.
     * <p>
     * {@link ModelMapper} используется для преобразования объектов одного типа в объекты другого типа
     * (например, для преобразования сущностей в DTO и наоборот).
     * </p>
     *
     * @return Новый экземпляр {@link ModelMapper}.
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
