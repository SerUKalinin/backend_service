package com.example.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на восстановление пароля.
 * Содержит только поле email, которое необходимо для отправки ссылки на сброс пароля.
 */
@Data
public class ForgotPasswordDto {

    /**
     * Email пользователя, запрашивающего восстановление пароля.
     * Обязательное поле. Должно быть в корректном формате email.
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;
}
