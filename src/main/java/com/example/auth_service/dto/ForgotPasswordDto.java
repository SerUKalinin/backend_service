package com.example.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на восстановление пароля пользователя.
 *
 * <p>Используется для инициирования процесса сброса пароля. Клиент передаёт
 * email пользователя, на который будет отправлена ссылка для установки нового пароля.</p>
 */
@Data
public class ForgotPasswordDto {

    /**
     * Электронная почта пользователя, запрашивающего восстановление пароля.
     * <p>Обязательное поле. Должно быть заполнено и соответствовать корректному
     * формату email. Используется для идентификации аккаунта и отправки ссылки
     * для сброса пароля.</p>
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;
}
