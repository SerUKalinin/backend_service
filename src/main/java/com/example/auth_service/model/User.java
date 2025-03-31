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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Сущность для представления пользователя.
 * Хранит информацию о пользователе, его ролях и постах.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    /**
     * Идентификатор пользователя.
     * Уникальный и генерируется автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя.
     * Уникально для каждого пользователя и не может быть пустым.
     */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /**
     * Электронная почта пользователя.
     * Уникальна для каждого пользователя и не может быть пустой.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Пароль пользователя.
     * Не может быть пустым.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Роли пользователя.
     * Связь "многие ко многим" с таблицей ролей.
     * Используется eager загрузка для немедленной загрузки ролей.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles;

    /**
     * Статус активации пользователя.
     * По умолчанию пользователь не активирован.
     */
    @Column(name = "active", nullable = false)
    private boolean active = false;  // Значение по умолчанию - false
}
