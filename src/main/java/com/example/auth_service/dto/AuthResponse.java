package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * DTO для передачи JWT-токена клиенту после успешной аутентификации.
 *
 * <p>Используется в ответах API при входе пользователя в систему
 * и содержит токен, необходимый для последующих авторизованных запросов.</p>
 */
@Getter
@Setter
@AllArgsConstructor
@Slf4j
public class AuthResponse {

    /**
     * JWT-токен, выданный пользователю после успешной аутентификации.
     */
    private String jwtToken;
}
