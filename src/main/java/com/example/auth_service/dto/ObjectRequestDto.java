package com.example.auth_service.dto;

import com.example.auth_service.model.ObjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для создания и обновления объекта недвижимости.
 * Используется для передачи данных от клиента при создании или изменении объектов в системе.
 */
@Data
public class ObjectRequestDto {

    /**
     * Название объекта недвижимости.
     * Не должно быть пустым.
     */
    @NotBlank(message = "Имя объекта не должно быть пустым")
    private String name;

    /**
     * Тип объекта недвижимости.
     * Обязательное поле. Например: ПРОЕКТ, ЗДАНИЕ, ЭТАЖ, КВАРТИРА и т.д.
     */
    @NotNull(message = "Тип объекта не должен быть null")
    private ObjectType objectType;

    /**
     * Идентификатор родительского объекта (если есть).
     * Может быть null, если объект находится на верхнем уровне и не имеет родителя.
     */
    private Long parentId;

    /**
     * Идентификатор ответственного пользователя за объект.
     * Может быть null, если ответственный ещё не назначен.
     */
    private Long responsibleUserId;
}
