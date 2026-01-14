package com.example.auth_service.dto;

import lombok.Data;

/**
 * DTO для передачи данных при назначении ответственного пользователя на задачу.
 *
 * <p>Используется в контроллере {@link com.example.auth_service.controller.TaskController}
 * для операций назначения ответственного пользователя.</p>
 */
@Data
public class AssignResponsibleRequest {

    /**
     * Идентификатор пользователя, который будет назначен ответственным за задачу.
     * <p>Должен соответствовать существующему пользователю в системе.</p>
     */
    private Long userId;
}
