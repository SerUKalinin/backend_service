package com.example.auth_service.dto;

import com.example.auth_service.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO для передачи данных пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * Имя пользователя.
     * Не может быть пустым или состоящим только из пробелов.
     */
    private String username;

    /**
     * Электронная почта пользователя.
     * Должна быть в формате email.
     */
    private String email;

    private String firstName;   // Добавлено поле для имени
    private String lastName;

    private Set<Role> roles;
}
