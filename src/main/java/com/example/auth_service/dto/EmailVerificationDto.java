package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для верификации электронной почты пользователя.
 *
 * <p>Используется в запросах API при подтверждении email после регистрации
 * или при повторной отправке кода подтверждения.</p>
 *
 * <p>Содержит адрес электронной почты пользователя и код верификации,
 * отправленный на указанный email.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationDto {

    /**
     * Электронная почта пользователя.
     *
     * <p>Должна соответствовать формату email.</p>
     */
    private String email;

    /**
     * Код верификации, отправленный пользователю.
     *
     * <p>Не должен быть пустым и используется для подтверждения владения email.</p>
     */
    private String code;
}
