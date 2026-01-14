package com.example.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на восстановление пароля пользователя.
 *
 * <p>Используется в запросах API для инициации процесса сброса пароля
 * путём отправки ссылки или кода восстановления на электронную почту пользователя.</p>
 */
@Data
public class ForgotPasswordDto {

    /**
     * Электронная почта пользователя, запрашивающего восстановление пароля.
     *
     * <p>Обязательное поле. Должно быть указано и соответствовать формату email.</p>
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;
}
