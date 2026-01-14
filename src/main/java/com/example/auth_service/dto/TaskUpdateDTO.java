package com.example.auth_service.dto;

import com.example.auth_service.model.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для обновления существующей задачи.
 *
 * <p>Используется для передачи данных от клиента при редактировании задачи в системе.
 * Содержит изменяемые поля задачи: заголовок, описание, статус и дедлайн.</p>
 */
@Data
public class TaskUpdateDTO {

    /**
     * Обновляемое название задачи.
     * Должно быть информативным, отражать суть задачи и содержать от 3 до 255 символов.
     */
    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    /**
     * Обновляемое описание задачи.
     * Поле может быть пустым, но длина строки не должна превышать 2000 символов.
     */
    @Size(max = 2000)
    private String description;

    /**
     * Обновляемый статус задачи.
     * Определяет текущее состояние задачи, например: ВЫПОЛНЕНА, В ПРОЦЕССЕ, НОВАЯ.
     */
    private TaskStatus status;

    /**
     * Обновляемый дедлайн задачи.
     * Устанавливает конечный срок выполнения задачи.
     * Должен быть текущим или будущим временем.
     */
    @FutureOrPresent
    private LocalDateTime deadline;
}
