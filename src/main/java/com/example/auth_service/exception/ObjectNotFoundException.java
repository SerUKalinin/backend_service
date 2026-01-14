package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке доступа к объекту недвижимости, который не существует в системе.
 *
 * <p>Используется, когда запрашиваемый объект недвижимости не найден в базе данных.</p>
 *
 * <p>Возвращает HTTP статус 404 NOT FOUND при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ObjectNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public ObjectNotFoundException(String message) {
        super(message);
    }
}
