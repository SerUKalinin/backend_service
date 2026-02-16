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

/**
 * REST-контроллер для управления процессами аутентификации и регистрации пользователей.
 *
 * Отвечает за регистрацию новых пользователей и администраторов, вход и выход из системы,
 * подтверждение email, сброс пароля, обновление и валидацию JWT токенов.
 * Контроллер делегирует бизнес-логику {@link AuthService} и управляет HTTP-ответами.
 * Применяет механизм {@link RateLimit} для защиты эндпоинтов от избыточной нагрузки.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    /**
     * Фасадный сервис для выполнения операций аутентификации и регистрации.
     */
    private final AuthService authService;

    /**
     * Утилита для работы с JWT токенами.
     */
    private final JwtUtil jwtUtil;

    /**
     * Сервис управления сессиями пользователей.
     */
    private final SessionService sessionService;

    /**
     * Регистрация нового пользователя.
     *
     * @param userSignupDto DTO с данными нового пользователя
     * @throws MessagingException если произошла ошибка при отправке письма подтверждения
     */
    @RateLimit(limit = 3, timeWindow = 3600)
    @PostMapping("/register-user")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody UserSignupDto userSignupDto) throws MessagingException {
        log.info("Регистрация пользователя: {}", userSignupDto.getEmail());
        authService.register(userSignupDto, false);
    }

    /**
     * Регистрация нового администратора.
     *
     * @param userSignupDto DTO с данными администратора
     * @throws MessagingException если произошла ошибка при отправке письма подтверждения
     */
    @RateLimit(limit = 1, timeWindow = 3600)
    @PostMapping("/register-admin")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAdmin(@Valid @RequestBody UserSignupDto userSignupDto) throws MessagingException {
        log.info("Регистрация администратора: {}", userSignupDto.getEmail());
        authService.register(userSignupDto, true);
    }

    /**
     * Аутентификация пользователя и выдача JWT токена.
     *
     * @param userSigninDto данные для входа (username/email и пароль)
     * @param response      HTTP-ответ для установки cookie с токенами при необходимости
     * @return {@link AuthResponse} с access токеном
     */
    @RateLimit(limit = 5, timeWindow = 60)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserSigninDto userSigninDto, HttpServletResponse response) {
        log.info("Аутентификация пользователя: {}", userSigninDto.getUsername());
        AuthResponse authResponse = authService.login(userSigninDto, response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Выход пользователя из системы и удаление сессии.
     *
     * @param request  HTTP-запрос с cookie refresh token
     * @param response HTTP-ответ для очистки cookie
     */
    @RateLimit(limit = 10, timeWindow = 60)
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Выход пользователя (refresh token из cookie)");
        authService.logout(request, response);
    }

    /**
     * Подтверждение email пользователя с кодом подтверждения.
     *
     * @param emailVerificationDto DTO с email и кодом подтверждения
     * @param response            HTTP-ответ для установки JWT cookie
     * @return {@link AuthResponse} с access токеном
     */
    @RateLimit(limit = 3, timeWindow = 60)
    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody EmailVerificationDto emailVerificationDto, HttpServletResponse response) {
        log.info("Проверка email: {}, code: {}", emailVerificationDto.getEmail(), emailVerificationDto.getCode());
        AuthResponse authResponse = authService.confirmEmail(emailVerificationDto.getEmail(), emailVerificationDto.getCode());
        authService.addJwtToCookie(authResponse.getJwtToken(), response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Повторная отправка кода подтверждения email пользователю.
     *
     * @param email email пользователя для повторной отправки
     * @throws MessagingException если произошла ошибка при отправке письма
     */
    @RateLimit(limit = 3, timeWindow = 3600)
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
    @RateLimit(limit = 10, timeWindow = 60)
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.refreshToken(request, response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Инициирует процесс сброса пароля пользователя.
     *
     * @param forgotPasswordDto DTO с email пользователя
     * @throws MessagingException если произошла ошибка при отправке письма
     */
    @RateLimit(limit = 3, timeWindow = 3600)
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) throws MessagingException {
        log.info("Запрос на сброс пароля для email: {}", forgotPasswordDto.getEmail());
        authService.sendPasswordResetLink(forgotPasswordDto.getEmail());
    }

    /**
     * Сброс пароля пользователя с установкой нового пароля.
     *
     * @param passwordResetDto DTO с токеном сброса и новым паролем
     * @return {@link AuthResponse} с новым access токеном после успешного сброса
     */
    @RateLimit(limit = 3, timeWindow = 3600)
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
     * @return HTTP 200 OK если токен валиден, HTTP 401 Unauthorized если токен невалиден
     */
    @RateLimit(limit = 10, timeWindow = 60)
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
