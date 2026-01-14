package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Сущность вложения к задаче.
 *
 * <p>Представляет файл, прикреплённый к задаче, с информацией о
 * оригинальном имени, пути хранения, размере и времени загрузки.</p>
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_attachments")
public class TaskAttachment {

    /** Уникальный идентификатор вложения. Генерируется автоматически. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Задача, к которой прикреплено вложение. Обязательное поле. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** Путь к сохранённому файлу на сервере или диске. Обязательное поле. */
    @Column(nullable = false)
    private String filePath;

    /** Оригинальное имя загруженного файла. Обязательное поле. */
    @Column(nullable = false)
    private String originalFileName;

    /** Размер файла в байтах. Обязательное поле. */
    @Column(nullable = false)
    private Long size;

    /** Дата и время загрузки файла. Устанавливается автоматически при сохранении. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Автоматическая установка даты и времени загрузки перед сохранением в базу данных.
     */
    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
