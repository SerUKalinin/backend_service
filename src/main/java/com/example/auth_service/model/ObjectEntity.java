package com.example.auth_service.model;

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
 * Универсальная сущность для всех объектов (проекты, здания, этажи, квартиры, задачи и т. д.).
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
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Тип объекта (проект, этаж, квартира, задача и т. д.).
     */
    @Column(name = "object_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ObjectType objectType;

    /**
     * Родительский объект (например, дом у проекта, квартира у дома).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ObjectEntity parent;

    /**
     * Дата создания объекта.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Дата последнего обновления объекта.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}