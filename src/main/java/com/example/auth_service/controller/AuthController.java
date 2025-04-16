package com.example.auth_service.controller;

import com.example.auth_service.annotation.RateLimit;
import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.UserSigninDto;
import com.example.auth_service.dto.UserSignupDto;
import com.example.auth_service.dto.EmailVerificationDto;
import com.example.auth_service.dto.PasswordResetDto;
import com.example.auth_service.dto.ForgotPasswordDto;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.SessionService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Duration;

/**
 * Контроллер для аутентификации и регистрации пользователей.
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
     * Ограничение: 3 запроса в час с одного IP
     *
     * @param userSignupDto данные пользователя для регистрации.
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/register-user")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody UserSignupDto userSignupDto) throws MessagingException {
        log.info("Регистрация пользователя: {}", userSignupDto.getEmail());
        authService.register(userSignupDto, false);  // false - для обычного пользователя
    }

    /**
     * Регистрация нового администратора.
     * Ограничение: 1 запрос в час с одного IP
     *
     * @param userSignupDto данные администратора для регистрации.
     */
    @RateLimit(value = 1, timeWindow = 3600)
    @PostMapping("/register-admin")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAdmin(@Valid @RequestBody UserSignupDto userSignupDto) throws MessagingException {
        log.info("Регистрация администратора: {}", userSignupDto.getEmail());
        authService.register(userSignupDto, true);  // true - для администратора
    }

    /**
     * Аутентификация пользователя.
     * Ограничение: 5 запросов в минуту с одного IP
     *
     * @param userSigninDto данные для входа.
     * @return JWT-токен.
     */
    @RateLimit(value = 5, timeWindow = 60)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserSigninDto userSigninDto, HttpServletResponse response) {
        log.info("Аутентификация пользователя: {}", userSigninDto.getUsername());

        // Выполняем логику входа через сервис
        AuthResponse authResponse = authService.login(userSigninDto);

        // Добавляем JWT в cookie
        authService.addJwtToCookie(authResponse.getJwtToken(), response);

        return ResponseEntity.ok(authResponse); // Возвращаем успешный ответ
    }

    /**
     * Выход пользователя из системы.
     * Ограничение: 10 запросов в минуту с одного IP
     */
    @RateLimit(value = 10, timeWindow = 60)
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Выход пользователя, токен: {}", authHeader);
        authService.logout(authHeader);
    }

    /**
     * Подтверждение email.
     * Ограничение: 3 запроса в минуту с одного IP
     */
    @RateLimit(value = 3, timeWindow = 60)
    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody EmailVerificationDto emailVerificationDto, HttpServletResponse response) {
        log.info("Получен запрос на проверку email: {}, code: {}", emailVerificationDto.getEmail(), emailVerificationDto.getCode());
        AuthResponse authResponse = authService.confirmEmail(emailVerificationDto.getEmail(), emailVerificationDto.getCode());

        // Добавляем JWT в cookie
        authService.addJwtToCookie(authResponse.getJwtToken(), response);

        return ResponseEntity.ok(authResponse); // Возвращаем токен в теле ответа и статус 200 OK
    }

    /**
     * Повторная отправка кода подтверждения на email.
     * Ограничение: 3 запроса в час с одного IP
     *
     * @param email адрес электронной почты пользователя.
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.OK)
    public void resendEmailVerification(@RequestParam String email) throws MessagingException {
        log.info("Запрос на повторную отправку кода подтверждения на email: {}", email);
        authService.resendConfirmationCode(email);
    }

    /**
     * Обновляет токен пользователя.
     * Ограничение: 10 запросов в минуту с одного IP
     *
     * @param authHeader Заголовок Authorization с текущим токеном.
     * @return ResponseEntity с новым токеном или ошибкой.
     */
    @RateLimit(value = 10, timeWindow = 60)
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Запрос на обновление токена");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
                String username = decodedJWT.getSubject();
                
                // Проверяем сессию
                if (sessionService.isSessionValid(username, token)) {
                    // Обновляем сессию
                    sessionService.refreshSession(username, Duration.ofHours(2));
                    log.info("Токен успешно обновлен для пользователя: {}", username);
                    return ResponseEntity.ok(new AuthResponse(token));
                }
            } catch (Exception e) {
                log.error("Ошибка при обновлении токена: {}", e.getMessage());
            }
        }
        log.warn("Не удалось обновить токен");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Отправляет ссылку для сброса пароля на email пользователя.
     * Ограничение: 3 запроса в час с одного IP
     *
     * @param forgotPasswordDto Данные для запроса сброса пароля.
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) throws MessagingException {
        log.info("Запрос на сброс пароля для email: {}", forgotPasswordDto.getEmail());
        authService.sendPasswordResetLink(forgotPasswordDto.getEmail());
    }

    /**
     * Сбрасывает пароль пользователя.
     * Ограничение: 3 запроса в час с одного IP
     *
     * @param passwordResetDto Данные для сброса пароля.
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
     * Проверяет валидность токена и возвращает информацию о пользователе.
     * Ограничение: 10 запросов в минуту с одного IP
     *
     * @param authHeader Заголовок Authorization с токеном.
     * @return ResponseEntity с информацией о пользователе или ошибкой.
     */
    @RateLimit(value = 10, timeWindow = 60)
    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Запрос на валидацию токена");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
                String username = decodedJWT.getSubject();
                
                // Проверяем сессию
                if (sessionService.isSessionValid(username, token)) {
                    // Обновляем сессию
                    sessionService.refreshSession(username, Duration.ofHours(2));
                    log.info("Токен успешно валидирован для пользователя: {}", username);
                    return ResponseEntity.ok().build();
                }
            } catch (Exception e) {
                log.error("Ошибка при валидации токена: {}", e.getMessage());
            }
        }
        log.warn("Не удалось валидировать токен");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
