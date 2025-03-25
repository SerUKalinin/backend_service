package com.example.auth_service.config.security.filter;

import com.example.auth_service.service.security.CustomUserDetailsService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр аутентификации, основанный на JWT.
 * Проверяет наличие и валидность JWT в заголовке Authorization.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Проверяет и обрабатывает JWT-токен из запроса.
     *
     * @param request     HTTP-запрос.
     * @param response    HTTP-ответ.
     * @param filterChain Цепочка фильтров.
     * @throws IOException      если произошла ошибка ввода-вывода.
     * @throws ServletException если произошла ошибка сервлета.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isBlank()) {
            log.debug("Заголовок Authorization отсутствует");
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Неверный формат заголовка Authorization");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header format");
            return;
        }

        String jwt = authHeader.substring(7);

        if (!jwtUtil.isValid(jwt)) {
            log.warn("Получен недействительный JWT-токен");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT in Bearer Header");
            return;
        }

        String username = jwtUtil.decodeToken(jwt).getSubject();
        if (username == null || username.isBlank()) {
            log.warn("JWT не содержит имя пользователя");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token does not contain a valid username");
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("Аутентификация уже установлена для пользователя: {}", username);
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails;
        try {
            // Используем loadUserByUsername, который теперь работает для логина и почты
            userDetails = customUserDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            log.error("Ошибка загрузки данных пользователя: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Пользователь не найден или не удалось выполнить проверку подлинности");
            return;
        }

        if (userDetails == null) {
            log.warn("Пользователь {} не найден", username);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Пользователь не найден");
            return;
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        log.info("Пользователь {} успешно аутентифицирован", username);

        filterChain.doFilter(request, response);
    }
}
