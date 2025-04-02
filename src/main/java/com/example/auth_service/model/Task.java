package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.NEW;

    /** Дата и время создания задачи. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Дедлайн выполнения задачи. */
    private LocalDateTime deadline;

    /** Объект недвижимости, к которому привязана задача. */
    @ManyToOne
    @JoinColumn(name = "object_id", nullable = false)
    private ObjectEntity realEstateObject;

    /** Список вложений к задаче. */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskAttachment> attachments;

    /**
     * Устанавливает дату создания задачи перед сохранением в базу данных.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
