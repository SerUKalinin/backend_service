package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для передачи информации о пользователе.
 *
 * <p>Используется для обмена данными о пользователе между слоями приложения,
 * а также для отображения информации о пользователе в интерфейсе.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * Уникальный идентификатор пользователя в системе.
     */
    private Long id;

    /**
     * Имя пользователя.
     *
     * <p>Не может быть пустым или состоять только из пробелов.</p>
     */
    private String username;

    /**
     * Электронная почта пользователя.
     *
     * <p>Должна быть в корректном формате email.</p>
     */
    private String email;

    /**
     * Имя пользователя.
     *
     * <p>Может быть пустым.</p>
     */
    private String firstName;

    /**
     * Фамилия пользователя.
     *
     * <p>Может быть пустой.</p>
     */
    private String lastName;

    /**
     * Роли пользователя в системе.
     *
     * <p>Например: ADMIN, USER, MANAGER.</p>
     */
    private List<String> roles;

    /**
     * Статус активации пользователя.
     *
     * <p>true — пользователь активирован, false — пользователь заблокирован.</p>
     */
    private boolean active;
}
