package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке создания задачи с названием, которое уже существует в системе.
 *
 * <p>Используется для предотвращения дублирования задач с одинаковыми названиями,
 * что обеспечивает уникальность идентификации задач и корректное отображение их в пользовательском интерфейсе.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class TaskAlreadyExistsException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message описание причины конфликта, содержащее информацию о существующей задаче
     */
    public TaskAlreadyExistsException(String message) {
        super(message);
    }
}
