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
 * Хранит основную информацию о пользователе, его ролях и статусе активации.
 * <p>
 * Используется для управления доступом и авторизацией в приложении.
 * Связана с таблицей "users" в базе данных.
 * </p>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически при сохранении в базе данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Уникальное имя пользователя для входа в систему.
     * Не может быть пустым.
     */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /**
     * Электронная почта пользователя.
     * Используется для связи и восстановления пароля.
     * Должна быть уникальной и не пустой.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Пароль пользователя.
     * Хранится в зашифрованном виде.
     * Не может быть пустым.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Имя пользователя.
     * Поле может быть пустым.
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * Фамилия пользователя.
     * Поле может быть пустым.
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * Набор ролей пользователя.
     * Связь "многие ко многим" с таблицей ролей (roles).
     * Используется для определения прав доступа пользователя.
     * Загружается eagerly при извлечении пользователя.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles;

    /**
     * Статус активации пользователя.
     * Если true, пользователь активен и может выполнять операции в системе.
     * По умолчанию false — пользователь не активирован.
     */
    @Column(name = "active", nullable = false)
    private boolean active = false;
}
