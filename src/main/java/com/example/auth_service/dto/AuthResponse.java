package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * DTO-объект для ответа с JWT-токеном после успешной аутентификации.
 */
@Getter
@Setter
@AllArgsConstructor
@Slf4j
public class AuthResponse {

    /**
     * JWT-токен, который клиент получает после успешной аутентификации.
     */
    private String jwtToken;

    /**
     * Refresh токен для обновления JWT-токена.
     */
    private String refreshToken;

    /**
     * Конструктор для обратной совместимости.
     */
    public AuthResponse(String jwtToken) {
        this.jwtToken = jwtToken;
        this.refreshToken = null;
    }
}
