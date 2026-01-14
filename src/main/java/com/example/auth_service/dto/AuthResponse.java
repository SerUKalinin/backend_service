package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * DTO для передачи JWT-токена клиенту после успешной аутентификации.
 *
 * <p>Используется в контроллерах аутентификации для формирования ответа клиенту
 * и передачи access токена, который будет использоваться для последующих запросов
 * к защищённым ресурсам системы.</p>
 */
@Getter
@Setter
@AllArgsConstructor
@Slf4j
public class AuthResponse {

    /**
     * JWT-токен, предоставляющий доступ к защищённым ресурсам системы.
     * <p>Клиент должен хранить этот токен безопасно и передавать его в заголовке
     * Authorization при последующих запросах.</p>
     */
    private String jwtToken;
}
