package com.example.auth_service.dto;

import com.example.auth_service.model.ObjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для создания и обновления объектов недвижимости.
 *
 * <p>Используется в запросах API для передачи данных от клиента
 * при создании новых объектов или изменении существующих в иерархии недвижимости.</p>
 */
@Data
public class ObjectRequestDto {

    /**
     * Название объекта недвижимости.
     *
     * <p>Обязательное поле. Не должно быть пустым.</p>
     */
    @NotBlank(message = "Имя объекта не должно быть пустым")
    private String name;

    /**
     * Тип объекта недвижимости.
     *
     * <p>Обязательное поле, определяющее уровень и роль объекта в иерархии
     * (например: ПРОЕКТ, ЗДАНИЕ, ЭТАЖ, КВАРТИРА и т.д.).</p>
     */
    @NotNull(message = "Тип объекта не должен быть null")
    private ObjectType objectType;

    /**
     * Идентификатор родительского объекта недвижимости.
     *
     * <p>Может быть {@code null}, если объект создаётся на верхнем уровне
     * и не имеет родителя.</p>
     */
    private Long parentId;

    /**
     * Идентификатор пользователя, ответственного за объект недвижимости.
     *
     * <p>Может быть {@code null}, если ответственный пользователь ещё не назначен.</p>
     */
    private Long responsibleUserId;
}
