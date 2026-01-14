package com.example.auth_service.dto;

import com.example.auth_service.model.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для обновления данных задачи.
 *
 * <p>Используется в запросах API при изменении существующей задачи.
 * Содержит поля, которые могут быть обновлены: название, описание, статус и дедлайн.</p>
 */
@Data
public class TaskUpdateDTO {

    /**
     * Название задачи.
     *
     * <p>Обновляемое поле. Должно быть не пустым и иметь длину от 3 до 255 символов.
     * Ясно отражает суть задачи.</p>
     */
    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    /**
     * Описание задачи.
     *
     * <p>Обновляемое поле. Может быть пустым, но не должно превышать 2000 символов.
     * Помогает понять, что необходимо выполнить для завершения задачи.</p>
     */
    @Size(max = 2000)
    private String description;

    /**
     * Статус задачи.
     *
     * <p>Обновляемое поле, отражает текущее состояние задачи
     * (например, ВЫПОЛНЕНА, В ПРОЦЕССЕ, ОЖИДАЕТ).</p>
     */
    private TaskStatus status;

    /**
     * Дедлайн задачи.
     *
     * <p>Обновляемое поле. Должен быть текущим моментом или датой в будущем.</p>
     */
    @FutureOrPresent
    private LocalDateTime deadline;
}
