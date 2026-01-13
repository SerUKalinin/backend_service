package com.example.auth_service.controller;

import com.example.auth_service.annotation.RateLimit;
import com.example.auth_service.dto.*;
import com.example.auth_service.service.auth.AuthService;
import com.example.auth_service.service.SessionService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * REST-контроллер для аутентификации и регистрации пользователей.
 * <p>
 * Обрабатывает все операции, связанные с:
 * <ul>
 *     <li>Регистрацией пользователей и администраторов</li>
 *     <li>Входом и выходом из системы</li>
 *     <li>Подтверждением и повторной отправкой кода email</li>
 *     <li>Сбросом пароля</li>
 *     <li>Обновлением и валидацией JWT токенов</li>
 * </ul>
 * Использует фасадный сервис {@link AuthService} для делегирования всех операций
 * на сервисы работы с аккаунтами и токенами.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final SessionService sessionService;

    /**
     * Регистрация нового пользователя.
     *
     * @param userSignupDto данные нового пользователя
     * @throws MessagingException если произошла ошибка при отправке письма подтверждения
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/register-user")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody UserSignupDto userSignupDto) throws MessagingException {
        log.info("Регистрация пользователя: {}", userSignupDto.getEmail());
        authService.register(userSignupDto, false);
    }

    /**
     * Регистрация нового администратора.
     *
     * @param userSignupDto данные администратора
     * @throws MessagingException если произошла ошибка при отправке письма подтверждения
     */
    @RateLimit(value = 1, timeWindow = 3600)
    @PostMapping("/register-admin")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAdmin(@Valid @RequestBody UserSignupDto userSignupDto) throws MessagingException {
        log.info("Регистрация администратора: {}", userSignupDto.getEmail());
        authService.register(userSignupDto, true);
    }

    /**
     * Аутентификация пользователя и получение JWT токена.
     *
     * @param userSigninDto данные для входа (username/email и пароль)
     * @param response      HTTP-ответ для возможной установки cookie
     * @return {@link AuthResponse} с access токеном
     */
    @RateLimit(value = 5, timeWindow = 60)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserSigninDto userSigninDto, HttpServletResponse response) {
        log.info("Аутентификация пользователя: {}", userSigninDto.getUsername());
        AuthResponse authResponse = authService.login(userSigninDto, response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Выход пользователя и удаление его сессии.
     *
     * @param request  HTTP-запрос с cookie refresh token
     * @param response HTTP-ответ для очистки cookie
     */
    @RateLimit(value = 10, timeWindow = 60)
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Выход пользователя (refresh token из cookie)");
        authService.logout(request, response);
    }

    /**
     * Подтверждение email пользователя с кодом.
     *
     * @param emailVerificationDto содержит email и код подтверждения
     * @param response            HTTP-ответ для установки JWT cookie
     * @return {@link AuthResponse} с access токеном
     */
    @RateLimit(value = 3, timeWindow = 60)
    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody EmailVerificationDto emailVerificationDto, HttpServletResponse response) {
        log.info("Проверка email: {}, code: {}", emailVerificationDto.getEmail(), emailVerificationDto.getCode());
        AuthResponse authResponse = authService.confirmEmail(emailVerificationDto.getEmail(), emailVerificationDto.getCode());
        authService.addJwtToCookie(authResponse.getJwtToken(), response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Повторная отправка кода подтверждения email.
     *
     * @param email email пользователя
     * @throws MessagingException если произошла ошибка при отправке письма
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.OK)
    public void resendEmailVerification(@RequestParam String email) throws MessagingException {
        log.info("Повторная отправка кода подтверждения на email: {}", email);
        authService.resendConfirmationCode(email);
    }

    /**
     * Обновление JWT токена на основе refresh token.
     *
     * @param request  HTTP-запрос с cookie refresh token
     * @param response HTTP-ответ для установки новых cookie
     * @return {@link AuthResponse} с новым access токеном
     */
    @RateLimit(value = 10, timeWindow = 60)
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.refreshToken(request, response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Запрос на сброс пароля пользователя.
     *
     * @param forgotPasswordDto содержит email пользователя
     * @throws MessagingException если произошла ошибка при отправке письма
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) throws MessagingException {
        log.info("Запрос на сброс пароля для email: {}", forgotPasswordDto.getEmail());
        authService.sendPasswordResetLink(forgotPasswordDto.getEmail());
    }

    /**
     * Сброс пароля пользователя.
     *
     * @param passwordResetDto содержит token и новый пароль
     * @return {@link AuthResponse} с новым access токеном
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
        log.info("Запрос на сброс пароля");
        AuthResponse authResponse = authService.resetPassword(passwordResetDto.getToken(), passwordResetDto.getNewPassword());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Валидация JWT токена.
     *
     * @param authHeader заголовок Authorization в формате "Bearer {token}"
     * @return HTTP 200 если токен валиден, HTTP 401 если нет
     */
    @RateLimit(value = 10, timeWindow = 60)
    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.validateJwtToken(token);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
