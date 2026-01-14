package com.example.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных пользователя при аутентификации.
 *
 * <p>Используется для приёма данных логина и пароля от клиента и последующей
 * передачи их в сервис аутентификации.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSigninDto {

    /**
     * Имя пользователя для входа в систему.
     * Обязательное поле. Должно содержать от 2 до 255 символов.
     */
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 255, message = "Имя пользователя должно содержать от 2 до 255 символов")
    private String username;

    /**
     * Пароль пользователя для входа.
     * Обязательное поле. Должен содержать от 6 до 255 символов.
     */
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 255, message = "Пароль должен содержать от 6 до 255 символов")
    private String password;
}
