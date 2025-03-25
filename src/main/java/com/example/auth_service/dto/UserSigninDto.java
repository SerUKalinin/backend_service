package com.example.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных для входа пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSigninDto {

    /**
     * Имя пользователя для входа.
     * Не может быть пустым и должно быть от 2 до 255 символов.
     */
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 255, message = "Имя пользователя должно содержать от 2 до 255 символов")
    private String username;

    /**
     * Пароль для входа.
     * Не может быть пустым и должен быть от 6 до 255 символов.
     */
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 255, message = "Пароль должен содержать от 6 до 255 символов")
    private String password;
}
