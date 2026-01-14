package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Сущность, представляющая вложение к задаче.
 *
 * <p>Хранит информацию о файле, его оригинальном имени, размере и времени загрузки,
 * а также связь с задачей, к которой относится вложение.</p>
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

    /** Путь к файлу на диске. */
    @Column(nullable = false)
    private String filePath;

    /** Оригинальное имя файла, загруженного пользователем. */
    @Column(nullable = false)
    private String originalFileName;

    /** Размер файла в байтах. */
    @Column(nullable = false)
    private Long size;

    /** Дата и время загрузки вложения. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Метод, вызываемый перед сохранением сущности.
     * Устанавливает дату загрузки вложения.
     */
    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
