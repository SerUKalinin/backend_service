package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке обращения к задаче, которая отсутствует в системе.
 *
 * <p>Используется для информирования сервисного и пользовательского слоев о том, что
 * запрашиваемая задача не найдена, что позволяет корректно обработать ситуацию
 * и вернуть соответствующий HTTP-статус {@link HttpStatus#NOT_FOUND}.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message подробное описание причины ошибки, например, информация о том, какая задача не найдена
     */
    public TaskNotFoundException(String message) {
        super(message);
    }
}
