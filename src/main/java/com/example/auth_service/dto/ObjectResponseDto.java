package com.example.auth_service.dto;

import com.example.auth_service.model.ObjectType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для передачи детализированной информации об объекте недвижимости.
 *
 * <p>Используется в ответах API для отображения структуры объекта,
 * а также данных о пользователях, связанных с его созданием и ответственностью.</p>
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
     * Тип объекта недвижимости.
     *
     * <p>Определяет уровень объекта в иерархии
     * (например: ПРОЕКТ, ЗДАНИЕ, ЭТАЖ, КВАРТИРА и т.д.).</p>
     */
    private ObjectType objectType;

    /**
     * Идентификатор родительского объекта недвижимости.
     *
     * <p>Может быть {@code null}, если объект находится на верхнем уровне.</p>
     */
    private Long parentId;

    /**
     * Дата и время создания объекта недвижимости.
     */
    private LocalDateTime createdAt;

    // ----------- Информация о пользователе, создавшем объект -----------

    /**
     * Идентификатор пользователя, создавшего объект недвижимости.
     */
    private Long createdById;

    /**
     * Имя пользователя, создавшего объект недвижимости.
     */
    private String createdByFirstName;

    /**
     * Фамилия пользователя, создавшего объект недвижимости.
     */
    private String createdByLastName;

    // ----------- Информация об ответственном пользователе -----------

    /**
     * Идентификатор ответственного пользователя за объект недвижимости.
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
     * Роль ответственного пользователя в системе.
     *
     * <p>Например: ПРОРАБ, ПОДРЯДЧИК, ДИРЕКТОР и т.д.</p>
     */
    private String responsibleUserRole;
}
