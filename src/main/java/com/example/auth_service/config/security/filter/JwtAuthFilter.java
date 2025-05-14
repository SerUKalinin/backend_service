package com.example.auth_service.config.security.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.auth_service.repository.redis.RedisRepository;
import com.example.auth_service.service.SessionService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import com.example.auth_service.service.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;

/**
 * Фильтр для аутентификации JWT.
 * Проверяет наличие и валидность JWT в заголовке Authorization.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisRepository redisRepository;
    private final SessionService sessionService;

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
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
                String username = decodedJWT.getSubject();

                // Проверяем, не истек ли токен
                if (decodedJWT.getExpiresAt().before(new Date())) {
                    log.warn("Токен истек");
                    throw new JWTVerificationException("Токен истек");
                }

                // Проверяем, не находится ли токен в черном списке
                if (redisRepository.isExists(token)) {
                    log.warn("Токен находится в черном списке");
                    throw new JWTVerificationException("Токен находится в черном списке");
                }

                // Проверяем валидность сессии
                if (!sessionService.isSessionValid(username, token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // Загружаем пользователя из базы данных
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // Создаем объект аутентификации
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Устанавливаем аутентификацию в контекст безопасности
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Обновляем сессию
                sessionService.updateSession(username, token, Duration.ofHours(2));

                log.debug("Успешная аутентификация для пользователя: {}", username);
            } catch (JWTVerificationException e) {
                log.error("Ошибка верификации JWT: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (Exception e) {
                log.error("Ошибка обработки JWT: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}