package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Сущность для хранения refresh-токенов пользователей.
 *
 * <p>Используется для управления долгоживущими токенами аутентификации
 * и реализации механизма обновления JWT без повторного ввода пароля.</p>
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    /**
     * Уникальный идентификатор refresh-токена.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя, которому принадлежит токен.
     */
    private String username;

    /**
     * Значение refresh-токена.
     */
    private String token;

    /**
     * Дата и время истечения срока действия токена.
     */
    private LocalDateTime expiresAt;

    /**
     * Дата и время создания токена.
     */
    private LocalDateTime createdAt;
}
