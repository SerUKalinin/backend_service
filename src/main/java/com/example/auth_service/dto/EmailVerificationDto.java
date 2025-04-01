package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DTO для верификации email.
 * Содержит email пользователя и код верификации.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationDto {

    /**
     * Электронная почта пользователя.
     * Должна быть в формате email.
     */
    private String email;

    /**
     * Код верификации, отправленный пользователю.
     * Не должен быть пустым.
     */
    private String code;
}
