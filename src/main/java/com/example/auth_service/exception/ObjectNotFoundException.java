package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке доступа к объекту недвижимости, которого нет в системе.
 *
 * <p>Используется для информирования, что запрашиваемый объект с указанным идентификатором отсутствует в базе данных.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ObjectNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message описание ошибки, содержащее информацию о причине отсутствия объекта
     */
    public ObjectNotFoundException(String message) {
        super(message);
    }
}
