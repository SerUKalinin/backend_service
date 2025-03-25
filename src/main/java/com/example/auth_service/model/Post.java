package com.example.auth_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность для представления поста.
 * Хранит информацию о контенте поста и привязанном пользователе.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
public class Post {

    /**
     * Идентификатор поста.
     * Уникальный и генерируется автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Контент поста.
     * Не может быть пустым.
     */
    @Column(name = "content", nullable = false)
    private String content;

    /**
     * Пользователь, который создал пост.
     * Связь с сущностью пользователя через поле user_id.
     * Для предотвращения ленивой загрузки может быть полезно использовать `fetch = FetchType.LAZY`.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
}
