package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке создать задачу с названием, которое уже используется.
 *
 * <p>Используется для предотвращения дублирования названий задач в системе.</p>
 *
 * <p>Возвращает HTTP статус 409 CONFLICT при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class TaskAlreadyExistsException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public TaskAlreadyExistsException(String message) {
        super(message);
    }
}
