package com.example.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных пользователя при регистрации.
 *
 * <p>Используется в запросах API для создания нового пользователя
 * с указанием имени пользователя, email, пароля и, при необходимости, имени и фамилии.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupDto {

    /**
     * Имя пользователя для регистрации.
     *
     * <p>Обязательное поле. Должно содержать от 2 до 255 символов.</p>
     */
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 255, message = "Имя пользователя должно содержать от 2 до 255 символов")
    private String username;

    /**
     * Электронная почта пользователя для регистрации.
     *
     * <p>Обязательное поле. Должна быть в корректном формате email
     * и содержать от 2 до 255 символов.</p>
     */
    @Email(message = "Неверный формат электронной почты")
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Size(min = 2, max = 255, message = "Электронная почта должна содержать от 2 до 255 символов")
    private String email;

    /**
     * Пароль пользователя для регистрации.
     *
     * <p>Обязательное поле. Должен содержать от 6 до 255 символов.</p>
     */
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 255, message = "Пароль должен содержать от 6 до 255 символов")
    private String password;

    /**
     * Имя пользователя.
     *
     * <p>Необязательное поле. Если указано, должно содержать от 2 до 255 символов.</p>
     */
    @Size(min = 2, max = 255, message = "Имя должно содержать от 2 до 255 символов")
    private String firstName;

    /**
     * Фамилия пользователя.
     *
     * <p>Необязательное поле. Если указано, должно содержать от 2 до 255 символов.</p>
     */
    @Size(min = 2, max = 255, message = "Фамилия должна содержать от 2 до 255 символов")
    private String lastName;
}
