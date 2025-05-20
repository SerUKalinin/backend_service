package com.example.auth_service.dto;

import com.example.auth_service.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для представления задачи.
 * Содержит информацию о задаче, включая её статус, описание и дедлайн.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    /**
     * Идентификатор задачи.
     * Уникальный идентификатор, который используется для извлечения и обновления задачи.
     */
    private Long id;

    /**
     * Название задачи.
     * Краткое описание задачи, которое должно быть уникальным и понятно отражать суть задачи.
     */
    private String title;

    /**
     * Описание задачи.
     * Подробное описание задачи, которое помогает понять, что необходимо выполнить.
     */
    private String description;

    /**
     * Статус задачи.
     * Отображает текущее состояние задачи (например, выполнена, в процессе, ожидает).
     */
    private TaskStatus status;

    /**
     * Дата и время создания задачи.
     * Время, когда задача была создана в системе.
     */
    private LocalDateTime createdAt;

    /**
     * Дедлайн задачи.
     * Время, к которому задача должна быть выполнена.
     */
    private LocalDateTime deadline;

    /**
     * Идентификатор объекта недвижимости, к которому привязана задача.
     * Уникальный идентификатор объекта недвижимости, для которого выполняется задача.
     */
    private Long realEstateObjectId;

    // Имя и фамилия создателя задачи
    private String createdByFirstName;
    private String createdByLastName;

    private Long responsibleUserId;
    private String responsibleUserFirstName;
    private String responsibleUserLastName;

}
