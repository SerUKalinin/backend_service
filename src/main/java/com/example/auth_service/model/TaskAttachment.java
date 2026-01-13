package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Сущность вложения к задаче.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_attachments")
public class TaskAttachment {

    /** Уникальный идентификатор вложения. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Задача, к которой прикреплено вложение. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** Сохранённое имя файла на диске */
    @Column(nullable = false)
    private String filePath;

    /** Оригинальное имя файла */
    @Column(nullable = false)
    private String originalFileName;

    /** Размер файла в байтах */
    @Column(nullable = false)
    private Long size;

    /** Дата и время загрузки вложения */
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Устанавливает дату загрузки перед сохранением в базу данных.
     */
    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
