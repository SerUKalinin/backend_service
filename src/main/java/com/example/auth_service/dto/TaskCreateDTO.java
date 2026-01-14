package com.example.auth_service.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для создания новой задачи в системе управления объектами недвижимости.
 *
 * <p>Используется для передачи от клиента информации о новой задаче,
 * включая заголовок, описание, дедлайн и объект недвижимости, к которому привязана задача.</p>
 */
@Data
public class TaskCreateDTO {

    /**
     * Заголовок задачи.
     * Обязательное поле. Длина строки должна быть от 3 до 255 символов.
     */
    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    /**
     * Подробное описание задачи.
     * Необязательное поле. Длина не должна превышать 2000 символов.
     */
    @Size(max = 2000)
    private String description;

    /**
     * Дата и время дедлайна задачи.
     * Обязательное поле. Должно быть текущим или будущим временем.
     */
    @FutureOrPresent
    private LocalDateTime deadline;

    /**
     * Идентификатор объекта недвижимости, к которому привязана задача.
     * Обязательное поле, не может быть null.
     */
    @NotNull
    private Long realEstateObjectId;
}
