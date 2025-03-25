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
 * Сущность для представления ролей пользователей.
 * Содержит информацию о типе роли (например, USER или ADMIN).
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role {

    /**
     * Идентификатор роли.
     * Уникальный и генерируется автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип роли.
     * Значение обязательно и должно быть уникальным.
     * Возможные типы: ROLE_USER, ROLE_ADMIN.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, unique = true)
    private RoleType roleType;

    /**
     * Перечисление для типов ролей.
     * Определяет доступные типы ролей в системе.
     */
    public enum RoleType {
        ROLE_USER,  // Роль пользователя с ограниченными правами
        ROLE_ADMIN  // Роль администратора с расширенными правами
    }
}
