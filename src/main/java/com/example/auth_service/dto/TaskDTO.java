package com.example.auth_service.dto;

import com.example.auth_service.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи информации о задаче.
 *
 * <p>Содержит все ключевые данные задачи, включая идентификаторы,
 * описание, статус, дедлайн, а также информацию о создателе и ответственном пользователе.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    /**
     * Уникальный идентификатор задачи.
     */
    private Long id;

    /**
     * Название задачи.
     *
     * <p>Краткое описание задачи, которое должно ясно отражать суть задачи.</p>
     */
    private String title;

    /**
     * Подробное описание задачи.
     *
     * <p>Помогает понять, что необходимо выполнить для завершения задачи.</p>
     */
    private String description;

    /**
     * Статус задачи.
     *
     * <p>Отражает текущее состояние задачи (например, ВЫПОЛНЕНА, В ПРОЦЕССЕ, ОЖИДАЕТ).</p>
     */
    private TaskStatus status;

    /**
     * Дата и время создания задачи в системе.
     */
    private LocalDateTime createdAt;

    /**
     * Дедлайн задачи.
     *
     * <p>Время, к которому задача должна быть выполнена.</p>
     */
    private LocalDateTime deadline;

    /**
     * Идентификатор объекта недвижимости, к которому привязана задача.
     */
    private Long realEstateObjectId;

    // ----------- Информация о создателе задачи -----------

    /**
     * Имя пользователя, создавшего задачу.
     */
    private String createdByFirstName;

    /**
     * Фамилия пользователя, создавшего задачу.
     */
    private String createdByLastName;

    // ----------- Информация об ответственном пользователе -----------

    /**
     * Идентификатор ответственного пользователя за задачу.
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
}
