package com.example.auth_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Сущность пользователя в системе.
 *
 * <p>Хранит основные данные о пользователе, включая логин, email, пароль,
 * роли и статус активации. Используется для аутентификации, авторизации
 * и управления доступом к функционалу системы.</p>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически при создании записи в базе данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Логин пользователя.
     * Уникальное значение, обязательное для заполнения.
     */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /**
     * Электронная почта пользователя.
     * Уникальное значение, обязательное для заполнения.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Пароль пользователя.
     * Хранится в зашифрованном виде и обязателен для заполнения.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Имя пользователя.
     * Может быть пустым.
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * Фамилия пользователя.
     * Может быть пустой.
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * Роли пользователя.
     * Используется для определения прав доступа и разграничения функционала.
     * Связь "многие ко многим" с сущностью {@link Role}.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles;

    /**
     * Статус активации пользователя.
     * Определяет, может ли пользователь выполнять действия в системе.
     * По умолчанию {@code false}.
     */
    @Column(name = "active", nullable = false)
    private boolean active = false;
}
