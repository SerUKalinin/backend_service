package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность для представления ролей пользователей.
 *
 * <p>Используется для назначения прав доступа пользователям системы.
 * Каждая роль имеет уникальный тип, например, ROLE_USER или ROLE_ADMIN.</p>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role {

    /**
     * Уникальный идентификатор роли.
     * Генерируется автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип роли.
     * Обязательное поле, уникальное для каждой роли.
     * Примеры: ROLE_USER, ROLE_ADMIN, ROLE_DIRECTOR и т.д.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, unique = true)
    private RoleType roleType;

    /**
     * Перечисление возможных типов ролей в системе.
     */
    public enum RoleType {
        /** Роль пользователя с базовыми правами. */
        ROLE_USER,

        /** Роль администратора системы. */
        ROLE_ADMIN,

        /** Роль директора. */
        ROLE_DIRECTOR,

        /** Роль начальника объекта. */
        ROLE_CHIEF,

        /** Роль ответственного пользователя/прораба. */
        ROLE_RESPONSIBLE
    }
}
