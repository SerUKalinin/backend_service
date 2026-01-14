package com.example.auth_service.service.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с безопасностью и контекстом аутентификации Spring Security.
 * <p>
 * Предоставляет методы для получения информации о текущем аутентифицированном пользователе
 * и интегрируется с {@link SecurityContextHolder}.
 * </p>
 */
@Service
public class SecurityService {

    /**
     * Возвращает имя (username) текущего аутентифицированного пользователя.
     * <p>
     * Использует {@link SecurityContextHolder} для извлечения данных аутентификации
     * из текущего контекста безопасности.
     * </p>
     *
     * @return Имя пользователя текущей сессии.
     * @throws IllegalStateException если контекст безопасности отсутствует или пользователь не аутентифицирован.
     */
    public String getCurrentUsername() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}
