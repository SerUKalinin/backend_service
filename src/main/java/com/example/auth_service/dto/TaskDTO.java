package com.example.auth_service.dto;

import com.example.auth_service.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи информации о задаче между клиентом и сервером.
 *
 * <p>Содержит все ключевые данные задачи, включая идентификатор, заголовок,
 * описание, статус, дедлайн, привязку к объекту недвижимости, а также информацию
 * о создателе и ответственном пользователе задачи.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    /**
     * Уникальный идентификатор задачи.
     * Используется для идентификации задачи в системе и ссылках на неё.
     */
    private Long id;

    /**
     * Заголовок задачи.
     * Краткое, информативное название задачи.
     */
    private String title;

    /**
     * Подробное описание задачи.
     * Помогает понять требования и детали выполнения задачи.
     */
    private String description;

    /**
     * Текущий статус задачи.
     * Определяет состояние задачи (например, ВЫПОЛНЕНА, В ПРОЦЕССЕ, НОВАЯ).
     */
    private TaskStatus status;

    /**
     * Дата и время создания задачи в системе.
     */
    private LocalDateTime createdAt;

    /**
     * Дата и время дедлайна задачи.
     * Определяет конечный срок выполнения задачи.
     */
    private LocalDateTime deadline;

    /**
     * Идентификатор объекта недвижимости, к которому привязана задача.
     * Обязательное поле для связи задачи с объектом.
     */
    private Long realEstateObjectId;

    /**
     * Имя пользователя, создавшего задачу.
     */
    private String createdByFirstName;

    /**
     * Фамилия пользователя, создавшего задачу.
     */
    private String createdByLastName;

    /**
     * Идентификатор пользователя, ответственного за выполнение задачи.
     * Может быть null, если ответственный не назначен.
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
