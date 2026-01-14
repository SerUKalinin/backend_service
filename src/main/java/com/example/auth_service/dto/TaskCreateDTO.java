package com.example.auth_service.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для создания задачи в системе.
 *
 * <p>Используется в запросах API при создании новой задачи,
 * связанной с объектом недвижимости или этапом выполнения работ.</p>
 */
@Data
public class TaskCreateDTO {

    /**
     * Название задачи.
     *
     * <p>Обязательное поле. Не может быть пустым.
     * Длина строки должна быть от 3 до 255 символов.</p>
     */
    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    /**
     * Описание задачи.
     *
     * <p>Необязательное поле. Может быть пустым,
     * но не должно превышать 2000 символов.</p>
     */
    @Size(max = 2000)
    private String description;

    /**
     * Дедлайн выполнения задачи.
     *
     * <p>Должен быть текущим моментом или датой в будущем.</p>
     */
    @FutureOrPresent
    private LocalDateTime deadline;

    /**
     * Идентификатор объекта недвижимости, к которому относится задача.
     *
     * <p>Обязательное поле. Используется для привязки задачи
     * к конкретному объекту в иерархии недвижимости.</p>
     */
    @NotNull
    private Long realEstateObjectId;
}
