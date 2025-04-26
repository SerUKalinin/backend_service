package com.example.auth_service.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для создания задачи.
 * Содержит информацию о задаче, которую пользователь хочет создать в системе.
 */
@Data
public class TaskCreateDTO {

    /**
     * Название задачи.
     * Не может быть пустым, длина строки должна быть от 3 до 255 символов.
     */
    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    /**
     * Описание задачи.
     * Описание задачи может быть пустым, но не может превышать 2000 символов.
     */
    @Size(max = 2000)
    private String description;

    /**
     * Дедлайн задачи.
     * Дедлайн должен быть текущим или в будущем.
     */
    @FutureOrPresent
    private LocalDateTime deadline;

    /**
     * Идентификатор объекта недвижимости, к которому привязана задача.
     * Это обязательное поле, и оно не может быть null.
     */
    @NotNull
    private Long realEstateObjectId;
}
