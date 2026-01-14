package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для подтверждения адреса электронной почты пользователя.
 *
 * <p>Используется при верификации email после регистрации или при
 * подтверждении смены почты. Содержит email и код подтверждения,
 * который был отправлен пользователю.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationDto {

    /**
     * Электронная почта пользователя.
     * <p>Должна быть валидным email-адресом и соответствовать аккаунту,
     * на который был отправлен код подтверждения.</p>
     */
    private String email;

    /**
     * Код подтверждения email.
     * <p>Должен быть получен пользователем из письма и передан без изменений.
     * Не может быть пустым или null.</p>
     */
    private String code;
}
