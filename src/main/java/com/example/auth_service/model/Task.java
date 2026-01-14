package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность задачи в системе.
 *
 * <p>Используется для управления задачами, привязанными к объектам недвижимости.
 * Хранит информацию о названии, описании, статусе, сроках, создателе, ответственном пользователе и вложениях.</p>
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Task {

    /** Уникальный идентификатор задачи. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Название задачи. */
    @Column(nullable = false)
    private String title;

    /** Описание задачи. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Статус задачи. */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.NEW;

    /** Дата и время создания задачи. */
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Дата и время последнего обновления задачи. */
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** Дедлайн выполнения задачи. */
    private LocalDateTime deadline;

    /** Объект недвижимости, к которому привязана задача. */
    @ManyToOne
    @JoinColumn(name = "object_id", nullable = false)
    private ObjectEntity realEstateObject;

    /** Список вложений к задаче. */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskAttachment> attachments = new ArrayList<>();

    /** Пользователь, создавший задачу. */
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    /** Ответственный пользователь за выполнение задачи. */
    @ManyToOne
    @JoinColumn(name = "responsible_user_id")
    private User responsibleUser;

    /**
     * Метод, вызываемый перед сохранением сущности.
     * Устанавливает даты создания и обновления.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Метод, вызываемый перед обновлением сущности.
     * Обновляет дату последнего изменения.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
