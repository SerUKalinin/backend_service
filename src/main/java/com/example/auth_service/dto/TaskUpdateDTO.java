package com.example.auth_service.dto;

import com.example.auth_service.model.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для обновления задачи.
 * Содержит данные, которые могут быть изменены при обновлении задачи.
 */
@Data
public class TaskUpdateDTO {

    /**
     * Название задачи.
     * Обновляемое название задачи, которое должно быть уникальным и понятно отражать суть задачи.
     * Должно быть не менее 3 символов и не более 255.
     */
    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    /**
     * Описание задачи.
     * Обновляемое описание задачи, которое помогает понять, что необходимо выполнить.
     * Максимальная длина 2000 символов.
     */
    @Size(max = 2000)
    private String description;

    /**
     * Статус задачи.
     * Обновляемый статус задачи (например, выполнена, в процессе, ожидает).
     */
    private TaskStatus status;

    /**
     * Дедлайн задачи.
     * Обновляемое время, к которому задача должна быть выполнена.
     * Дедлайн должен быть в будущем или в текущий момент.
     */
    @FutureOrPresent
    private LocalDateTime deadline;
}
