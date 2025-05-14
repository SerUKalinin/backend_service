package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных пользователя.
 * Используется для обмена данными о пользователе между слоями приложения (например, для отображения информации о пользователе).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * Идентификатор пользователя.
     * Уникальный идентификатор пользователя в системе.
     */
    private Long id;

    /**
     * Имя пользователя.
     * Имя пользователя. Не может быть пустым или состоящим только из пробелов.
     */
    private String username;

    /**
     * Электронная почта пользователя.
     * Электронная почта пользователя. Должна быть в формате email.
     */
    private String email;

    /**
     * Имя пользователя.
     * Имя пользователя. Может быть пустым.
     */
    private String firstName;

    /**
     * Фамилия пользователя.
     * Фамилия пользователя. Может быть пустым.
     */
    private String lastName;

    /**
     * Роль пользователя.
     * Роль пользователя в системе (например, ADMIN, USER, MANAGER).
     */
    private String roles;

    /**
     * Статус активации пользователя.
     * Статус активации пользователя: true - пользователь активирован, false - заблокирован.
     */
    private boolean active;
}
