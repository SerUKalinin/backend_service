package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при неверном коде подтверждения пользователя.
 *
 * <p>Используется в процессах регистрации, подтверждения email и восстановления пароля,
 * когда предоставленный пользователем код подтверждения не совпадает с ожидаемым.</p>
 *
 * <p>Возникает до изменения состояния пользователя или выполнения безопасных операций.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidConfirmationCodeException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message описание ошибки, объясняющее причину некорректного кода подтверждения
     */
    public InvalidConfirmationCodeException(String message) {
        super(message);
    }
}
