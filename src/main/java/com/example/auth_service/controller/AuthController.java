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
import jakarta.servlet.http.HttpServletRequest;
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
 * <p>
 * Этот контроллер предоставляет REST-эндпоинты для регистрации пользователей и администраторов, входа в систему,
 * выхода, подтверждения email, обновления JWT-токена, сброса пароля и проверки токена.
 * Также используется аннотация {@link RateLimit} для ограничения количества запросов.
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
     * Регистрирует нового пользователя.
     * <p>
     * Отправляет код подтверждения на email.
     * Доступ ограничен: не более 3 запросов в час с одного IP.
     * </p>
     *
     * @param userSignupDto DTO с данными для регистрации пользователя.
     * @throws MessagingException если не удалось отправить email.
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/register-user")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody UserSignupDto userSignupDto) throws MessagingException {
        log.info("Регистрация пользователя: {}", userSignupDto.getEmail());
        authService.register(userSignupDto, false);  // false - для обычного пользователя
    }

    /**
     * Регистрирует нового администратора.
     * <p>
     * Администратор отличается флагом "isAdmin = true".
     * Доступ ограничен: не более 1 запроса в час с одного IP.
     * </p>
     *
     * @param userSignupDto DTO с данными администратора.
     * @throws MessagingException если не удалось отправить email.
     */
    @RateLimit(value = 1, timeWindow = 3600)
    @PostMapping("/register-admin")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAdmin(@Valid @RequestBody UserSignupDto userSignupDto) throws MessagingException {
        log.info("Регистрация администратора: {}", userSignupDto.getEmail());
        authService.register(userSignupDto, true);  // true - для администратора
    }

    /**
     * Аутентифицирует пользователя по логину и паролю.
     * <p>
     * Возвращает JWT-токен в теле ответа и в cookie.
     * Доступ ограничен: не более 5 запросов в минуту с одного IP.
     * </p>
     *
     * @param userSigninDto DTO с данными для входа.
     * @param response HTTP-ответ, в который добавляется cookie с токеном.
     * @return Ответ с JWT-токеном.
     */
    @RateLimit(value = 5, timeWindow = 60)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserSigninDto userSigninDto, HttpServletResponse response) {
        log.info("Аутентификация пользователя: {}", userSigninDto.getUsername());
        AuthResponse authResponse = authService.login(userSigninDto, response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Выходит из системы, удаляя токен из Redis.
     * <p>
     * Доступ ограничен: не более 10 запросов в минуту с одного IP.
     * </p>
     *
     * @param request HTTP-запрос, содержащий cookie с токеном.
     * @param response HTTP-ответ, в который удаляется cookie с токеном.
     */
    @RateLimit(value = 10, timeWindow = 60)
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Выход пользователя (refresh token из cookie)");
        authService.logout(request, response);
    }

    /**
     * Подтверждает email пользователя по коду.
     * <p>
     * Возвращает JWT-токен в теле и в cookie.
     * Доступ ограничен: не более 3 запросов в минуту с одного IP.
     * </p>
     *
     * @param emailVerificationDto DTO с email и кодом подтверждения.
     * @param response HTTP-ответ, в который добавляется cookie с токеном.
     * @return Ответ с JWT-токеном.
     */
    @RateLimit(value = 3, timeWindow = 60)
    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody EmailVerificationDto emailVerificationDto, HttpServletResponse response) {
        log.info("Получен запрос на проверку email: {}, code: {}", emailVerificationDto.getEmail(), emailVerificationDto.getCode());
        AuthResponse authResponse = authService.confirmEmail(emailVerificationDto.getEmail(), emailVerificationDto.getCode());
        authService.addJwtToCookie(authResponse.getJwtToken(), response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Повторно отправляет код подтверждения на указанный email.
     * <p>
     * Доступ ограничен: не более 3 запросов в час с одного IP.
     * </p>
     *
     * @param email Адрес электронной почты пользователя.
     * @throws MessagingException если не удалось отправить email.
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.OK)
    public void resendEmailVerification(@RequestParam String email) throws MessagingException {
        log.info("Запрос на повторную отправку кода подтверждения на email: {}", email);
        authService.resendConfirmationCode(email);
    }

    /**
     * Обновляет JWT-токен пользователя, если токен валиден и сессия активна.
     * <p>
     * Доступ ограничен: не более 10 запросов в минуту с одного IP.
     * </p>
     *
     * @param request HTTP-запрос, содержащий cookie с токеном.
     * @param response HTTP-ответ, в который добавляется cookie с новым токеном.
     * @return Ответ с новым токеном или статус 401, если обновление невозможно.
     */
    @RateLimit(value = 10, timeWindow = 60)
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponse authResponse = authService.refreshAccessToken(refreshToken, response);
        if (authResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Отправляет ссылку на сброс пароля на указанный email.
     * <p>
     * Доступ ограничен: не более 3 запросов в час с одного IP.
     * </p>
     *
     * @param forgotPasswordDto DTO с email пользователя.
     * @throws MessagingException если не удалось отправить email.
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) throws MessagingException {
        log.info("Запрос на сброс пароля для email: {}", forgotPasswordDto.getEmail());
        authService.sendPasswordResetLink(forgotPasswordDto.getEmail());
    }

    /**
     * Сбрасывает пароль пользователя по токену из письма.
     * <p>
     * Доступ ограничен: не более 3 запросов в час с одного IP.
     * </p>
     *
     * @param passwordResetDto DTO с токеном и новым паролем.
     * @return Ответ с JWT-токеном.
     */
    @RateLimit(value = 3, timeWindow = 3600)
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
        log.info("Запрос на сброс пароля");
        AuthResponse authResponse = authService.resetPassword(passwordResetDto.getToken(), passwordResetDto.getNewPassword());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Проверяет валидность переданного JWT-токена.
     * <p>
     * Если токен валиден и сессия активна, возвращается статус 200 OK.
     * Иначе — статус 401 Unauthorized.
     * Доступ ограничен: не более 10 запросов в минуту с одного IP.
     * </p>
     *
     * @param authHeader Заголовок Authorization с токеном.
     * @return Статус 200 OK при успехе или 401 Unauthorized.
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

                if (sessionService.isSessionValid(username, token)) {
                    sessionService.updateSession(username, token, Duration.ofHours(2));
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

