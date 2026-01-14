package com.example.auth_service.service.auth;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.exception.AuthException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.SessionService;
import com.example.auth_service.service.redis.RedisService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Сервис для управления токенами аутентификации пользователей.
 *
 * <p>Обеспечивает обновление access и refresh токенов, управление JWT в HttpOnly cookie,
 * выполнение выхода пользователя и проверку валидности токенов с поддержкой сессий.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTokenService {

    /**
     * Сервис для работы с refresh токенами в Redis.
     */
    private final RedisService redisService;

    /**
     * Сервис для работы с сессиями пользователей.
     */
    private final SessionService sessionService;

    /**
     * Утилита для генерации и декодирования JWT-токенов.
     */
    private final JwtUtil jwtUtil;

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Обновляет access token на основе переданного refresh token и сохраняет новые токены.
     *
     * @param refreshToken refresh token пользователя, должен быть действительным
     * @param response     HTTP-ответ для установки нового refresh token в cookie
     * @return {@link AuthResponse} с новым access JWT-токеном
     * @throws AuthException если refresh token недействителен или пользователь не найден
     */
    public AuthResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        String username = redisService.findUsernameByRefreshToken(refreshToken);

        if (username == null || !redisService.isRefreshTokenValid(username, refreshToken)) {
            throw new AuthException("Refresh token недействителен");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("Пользователь не найден"));

        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken();

        redisService.deleteRefreshToken(username, refreshToken);
        persistTokens(username, newAccessToken, newRefreshToken, response);

        return new AuthResponse(newAccessToken);
    }

    /**
     * Обновляет access token на основе refresh token из HTTP-запроса.
     *
     * @param request  HTTP-запрос с cookie refresh token
     * @param response HTTP-ответ для установки нового refresh token
     * @return {@link AuthResponse} с новым access JWT-токеном
     * @throws AuthException если refresh token отсутствует или недействителен
     */
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            throw new AuthException("Refresh token отсутствует");
        }

        return refreshAccessToken(refreshToken, response);
    }

    /**
     * Выполняет выход пользователя, удаляя refresh token из Redis и сессию,
     * а также удаляя cookie с refresh token.
     *
     * @param request  HTTP-запрос с cookie
     * @param response HTTP-ответ для удаления cookie
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken != null) {
            String username = redisService.findUsernameByRefreshToken(refreshToken);
            if (username != null) {
                redisService.deleteRefreshToken(username, refreshToken);
                sessionService.removeSession(username);
            }
        }

        invalidateRefreshCookie(response);
    }

    /**
     * Добавляет JWT-токен в HttpOnly cookie для безопасного хранения на клиенте.
     *
     * @param token    JWT-токен
     * @param response HTTP-ответ для установки cookie
     */
    public void addJwtToCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 2); // 2 часа
        response.addCookie(cookie);
    }

    /**
     * Валидирует JWT-токен, проверяет сессию пользователя и обновляет время её действия.
     *
     * @param token JWT-токен для проверки
     * @throws AuthException если токен недействителен или сессия отсутствует
     */
    public void validateJwtToken(String token) {
        DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
        String username = decodedJWT.getSubject();
        if (!sessionService.isSessionValid(username, token)) {
            throw new AuthException("Токен недействителен");
        }
        sessionService.updateSession(username, token, Duration.ofHours(2));
    }

    // ------------------- приватные вспомогательные методы -------------------

    /**
     * Генерирует новый access JWT-токен для пользователя.
     *
     * @param user пользователь
     * @return новый access JWT-токен
     */
    private String generateAccessToken(User user) {
        return jwtUtil.generateToken(
                user,
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getRoleType().name()))
                        .toList()
        );
    }

    /**
     * Генерирует новый refresh token.
     *
     * @return новый refresh token
     */
    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Сохраняет access и refresh токены в Redis и устанавливает cookie с refresh token.
     *
     * @param username имя пользователя
     * @param access   access token
     * @param refresh  refresh token
     * @param response HTTP-ответ для установки cookie
     */
    private void persistTokens(String username, String access, String refresh, HttpServletResponse response) {
        redisService.saveRefreshToken(username, refresh, Duration.ofDays(7));
        sessionService.saveSession(username, access, Duration.ofHours(2));

        Cookie cookie = new Cookie("refreshToken", refresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) Duration.ofDays(7).getSeconds());
        response.addCookie(cookie);
    }

    /**
     * Извлекает refresh token из cookie HTTP-запроса.
     *
     * @param request HTTP-запрос
     * @return refresh token или null, если отсутствует
     */
    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if ("refreshToken".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    /**
     * Удаляет refresh token cookie на клиенте.
     *
     * @param response HTTP-ответ для удаления cookie
     */
    private void invalidateRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
