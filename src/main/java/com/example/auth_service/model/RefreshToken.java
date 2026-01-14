package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Сущность для хранения refresh-токенов пользователей.
 *
 * <p>Используется для реализации механизма обновления JWT токенов. Каждый
 * токен привязан к конкретному пользователю и имеет время действия. Хранение
 * refresh-токенов позволяет безопасно выдавать новые access-токены без повторной аутентификации.</p>
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
     * Используется для поиска токена по пользователю при обновлении access-токена.
     */
    private String username;

    /**
     * Сам refresh-токен.
     * Строка, генерируемая сервисом аутентификации, используемая для продления сессии.
     */
    private String token;

    /**
     * Дата и время истечения действия токена.
     * После этого времени токен считается недействительным и не может использоваться для получения нового access-токена.
     */
    private LocalDateTime expiresAt;

    /**
     * Дата и время создания токена.
     * Используется для аудита и контроля жизненного цикла токена.
     */
    private LocalDateTime createdAt;
}
