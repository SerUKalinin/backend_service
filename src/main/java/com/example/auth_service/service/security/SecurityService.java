package com.example.auth_service.service.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с безопасностью и контекстом аутентификации Spring Security.
 * <p>
 * Предоставляет методы для получения информации о текущем аутентифицированном пользователе.
 * </p>
 */
@Service
public class SecurityService {

    /**
     * Возвращает имя пользователя, который в данный момент аутентифицирован в системе.
     *
     * @return имя текущего пользователя (username)
     * @throws IllegalStateException если пользователь не аутентифицирован
     */
    public String getCurrentUsername() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}
