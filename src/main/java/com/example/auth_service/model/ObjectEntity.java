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
 * Универсальная сущность {@code ObjectEntity} для представления всех типов объектов в системе управления
 * недвижимостью и строительными проектами, включая проекты, здания, этажи, квартиры, комнаты, лестничные пролеты,
 * лифты, коридоры и задачи.
 *
 * <p>Обеспечивает хранение информации о названии, типе, родительском объекте, датах создания и обновления,
 * а также пользователях, создавших и ответственных за объект.</p>
 *
 * <p>Используется в сервисах управления объектами для построения иерархии объектов,
 * отображения структуры проекта и назначения ответственных пользователей.</p>
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
     * Генерируется автоматически базой данных.
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
     * Тип объекта, определяющий его роль в иерархии (например, проект, этаж, квартира, задача).
     * Сохраняется в базе как строковое значение.
     */
    @Column(name = "object_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private ObjectType objectType;

    /**
     * Родительский объект в иерархии.
     * Используется для построения дерева объектов.
     * Может быть null для верхнеуровневых объектов.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ObjectEntity parent;

    /**
     * Дата и время создания объекта.
     * Устанавливается автоматически при создании.
     * Не обновляется после создания.
     */
    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Дата и время последнего обновления объекта.
     * Обновляется при изменении сущности.
     */
    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Пользователь, создавший объект.
     * Может быть null, если объект создан системой или импортирован.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * Пользователь, ответственный за объект.
     * Используется для назначения задач и уведомлений.
     * Может быть null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private User responsibleUser;

    /**
     * Конструктор для создания объекта с известным идентификатором.
     * Используется в случаях, когда требуется ссылка на существующий объект без полной загрузки.
     *
     * @param id идентификатор существующего объекта
     */
    public ObjectEntity(Long id) {
        this.id = id;
    }
}
