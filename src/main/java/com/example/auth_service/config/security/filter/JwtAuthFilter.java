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
 * JWT-фильтр аутентификации для Spring Security.
 * <p>
 * Отвечает за извлечение, проверку и обработку JWT-токенов из HTTP-запросов.
 * Проверяет срок действия токена, его наличие в blacklist и валидность пользовательской сессии.
 * При успешной верификации устанавливает аутентификацию в {@link SecurityContextHolder}.
 * </p>
 * <p>
 * Используется на уровне security-фильтров для защиты REST-эндпоинтов приложения.
 * Поддерживает обновление времени жизни сессии после успешной аутентификации.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * Утилита для работы с JWT-токенами.
     * <p>
     * Предоставляет методы для декодирования и проверки валидности токенов.
     */
    private final JwtUtil jwtUtil;

    /**
     * Сервис для загрузки деталей пользователя.
     * <p>
     * Используется для извлечения {@link UserDetails} по имени пользователя.
     */
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Репозиторий Redis для работы с blacklist токенов.
     * <p>
     * Используется для проверки, что токен не был отозван.
     */
    private final RedisRepository redisRepository;

    /**
     * Сервис управления сессиями пользователей.
     * <p>
     * Проверяет валидность сессии и обновляет время её жизни после аутентификации.
     */
    private final SessionService sessionService;

    /**
     * Обрабатывает HTTP-запрос и выполняет JWT-аутентификацию при наличии токена.
     * <p>
     * Если токен действителен, устанавливает {@link UsernamePasswordAuthenticationToken}
     * в {@link SecurityContextHolder}. Обновляет срок действия сессии.
     * В случае ошибки верификации или истечения срока действия возвращает статус 401.
     * </p>
     *
     * @param request HTTP-запрос клиента; не может быть null
     * @param response HTTP-ответ, через который возвращаются коды ошибок при неуспешной аутентификации
     * @param filterChain цепочка фильтров Spring Security
     * @throws IOException при ошибках ввода-вывода
     * @throws ServletException при ошибках обработки запроса
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

                if (decodedJWT.getExpiresAt().before(new Date())) {
                    log.warn("Токен истек");
                    throw new JWTVerificationException("Токен истек");
                }

                if (redisRepository.isExists(token)) {
                    log.warn("Токен находится в черном списке");
                    throw new JWTVerificationException("Токен находится в черном списке");
                }

                if (!sessionService.isSessionValid(username, token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

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
