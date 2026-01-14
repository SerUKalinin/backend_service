package com.example.auth_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность, представляющая роль пользователя в системе.
 *
 * <p>Используется для управления доступом пользователей к различным функциям приложения.
 * Каждая роль имеет уникальный тип, определяющий уровень привилегий.</p>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role {

    /**
     * Уникальный идентификатор роли.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип роли.
     * Значение обязательно, уникально и определяет уровень доступа пользователя.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, unique = true)
    private RoleType roleType;

    /**
     * Перечисление допустимых типов ролей в системе.
     */
    public enum RoleType {
        /** Роль обычного пользователя с ограниченными правами */
        ROLE_USER,

        /** Роль администратора с расширенными правами */
        ROLE_ADMIN,

        /** Роль директора */
        ROLE_DIRECTOR,

        /** Роль начальника объекта */
        ROLE_CHIEF,

        /** Роль ответственного пользователя */
        ROLE_RESPONSIBLE
    }
}
