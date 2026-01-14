package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность задачи в системе управления недвижимостью и строительством.
 *
 * <p>Представляет задачу с названием, описанием, статусом, сроками выполнения,
 * привязкой к объекту недвижимости, ответственными пользователями и вложениями.</p>
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Task {

    /** Уникальный идентификатор задачи. Генерируется автоматически. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Название задачи. Обязательное поле. */
    @Column(nullable = false)
    private String title;

    /** Подробное описание задачи. Может быть пустым. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Статус задачи. По умолчанию NEW. */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.NEW;

    /** Дата и время создания задачи. Устанавливается автоматически при создании. */
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Дата и время последнего обновления задачи. Обновляется автоматически. */
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** Дата и время дедлайна выполнения задачи. Может быть null. */
    private LocalDateTime deadline;

    /** Объект недвижимости, к которому привязана задача. Обязательное поле. */
    @ManyToOne
    @JoinColumn(name = "object_id", nullable = false)
    private ObjectEntity realEstateObject;

    /** Список вложений к задаче. Управляется каскадно и поддерживает удаление сирот. */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskAttachment> attachments = new ArrayList<>();

    /** Пользователь, создавший задачу. */
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    /** Пользователь, ответственный за выполнение задачи. */
    @ManyToOne
    @JoinColumn(name = "responsible_user_id")
    private User responsibleUser;

    /**
     * Автоматическая установка дат создания и обновления перед сохранением в базу.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Автоматическое обновление даты последнего изменения перед обновлением сущности.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
