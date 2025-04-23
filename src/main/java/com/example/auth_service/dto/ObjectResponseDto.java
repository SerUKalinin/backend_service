package com.example.auth_service.dto;

import com.example.auth_service.model.ObjectType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для ответа с информацией об объекте недвижимости.
 * Используется для возврата детализированных данных об объекте клиенту.
 */
@Data
public class ObjectResponseDto {

    /**
     * Уникальный идентификатор объекта недвижимости.
     */
    private Long id;

    /**
     * Название объекта недвижимости.
     */
    private String name;

    /**
     * Тип объекта (например: ПРОЕКТ, ЗДАНИЕ, ЭТАЖ, КВАРТИРА и т.д.).
     */
    private ObjectType objectType;

    /**
     * Идентификатор родительского объекта.
     * Может быть null, если объект находится на верхнем уровне.
     */
    private Long parentId;

    /**
     * Дата и время создания объекта.
     */
    private LocalDateTime createdAt;

    // ----------- Информация о пользователе, создавшем объект -----------

    /**
     * Идентификатор пользователя, создавшего объект.
     */
    private Long createdById;

    /**
     * Имя пользователя, создавшего объект.
     */
    private String createdByFirstName;

    /**
     * Фамилия пользователя, создавшего объект.
     */
    private String createdByLastName;

    // ----------- Информация об ответственном пользователе -----------

    /**
     * Идентификатор ответственного пользователя.
     */
    private Long responsibleUserId;

    /**
     * Имя ответственного пользователя.
     */
    private String responsibleUserFirstName;

    /**
     * Фамилия ответственного пользователя.
     */
    private String responsibleUserLastName;

    /**
     * Роль ответственного пользователя (например: ПРОРАБ, ПОДРЯДЧИК и т.д.).
     */
    private String responsibleUserRole;
}
