package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при попытке подтверждения действия с использованием некорректного кода подтверждения.
 *
 * <p>Используется, например, при верификации email или сбросе пароля,
 * когда предоставленный пользователем код не совпадает с ожидаемым.</p>
 *
 * <p>Возвращает HTTP статус 400 BAD REQUEST при выбрасывании в контроллере.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidConfirmationCodeException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public InvalidConfirmationCodeException(String message) {
        super(message);
    }
}
