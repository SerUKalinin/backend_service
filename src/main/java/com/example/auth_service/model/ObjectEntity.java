package com.example.auth_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Универсальная сущность для представления объектов недвижимости и связанных сущностей.
 *
 * <p>Используется для проектов, зданий, этажей, квартир, комнат, лестничных пролетов, лифтов,
 * коридоров, холлов лифтов, балконов, задач и других объектов в иерархии недвижимости.</p>
 *
 * <p>Содержит информацию о названии объекта, типе, родительском объекте, датах создания и обновления,
 * а также пользователях, создавших объект и ответственных за него.</p>
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "objects")
public class ObjectEntity {

    /**
     * Уникальный идентификатор объекта.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название объекта.
     * Не может быть пустым.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Тип объекта.
     * Например: ПРОЕКТ, ЗДАНИЕ, ЭТАЖ, КВАРТИРА, ЗАДАЧА и т.д.
     */
    @Column(name = "object_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private ObjectType objectType;

    /**
     * Родительский объект.
     * Может быть null для объектов верхнего уровня (например, проект без родителя).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ObjectEntity parent;

    /**
     * Дата и время создания объекта.
     * Устанавливается при создании сущности и не обновляется.
     */
    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Дата и время последнего обновления объекта.
     * Обновляется при изменении сущности.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Пользователь, создавший объект.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * Пользователь, ответственный за объект.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private User responsibleUser;

    /**
     * Конструктор для создания объекта с указанием только ID.
     *
     * <p>Удобен для ссылок на объекты без необходимости загрузки полной сущности.</p>
     *
     * @param id идентификатор объекта
     */
    public ObjectEntity(Long id) {
        this.id = id;
    }
}