package com.example.auth_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** Путь к файлу вложения. */
    @Column(nullable = false)
    private String filePath;

    /** Дата и время загрузки вложения. */
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
