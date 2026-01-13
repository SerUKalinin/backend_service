package com.example.auth_service.service.auth;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.UserSigninDto;
import com.example.auth_service.dto.UserSignupDto;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервисный класс для работы с аутентификацией и управлением аккаунтами пользователей.
 * <p>
 * Делегирует работу с учётными записями {@link AuthAccountService} и работу с токенами {@link AuthTokenService}.
 * Обеспечивает регистрацию, авторизацию, подтверждение email, сброс пароля, выход из системы и обновление JWT-токенов.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthAccountService accountService;
    private final AuthTokenService authTokenService;

    /**
     * Регистрирует нового пользователя или администратора.
     *
     * @param dto     DTO с данными для регистрации пользователя.
     * @param isAdmin true — если регистрируется администратор, false — обычный пользователь.
     * @throws MessagingException если не удалось отправить email с кодом подтверждения.
     */
    public void register(UserSignupDto dto, boolean isAdmin) throws MessagingException {
        accountService.register(dto, isAdmin);
    }

    /**
     * Аутентифицирует пользователя и возвращает JWT-токен.
     *
     * @param dto      DTO с логином и паролем пользователя.
     * @param response HTTP-ответ для установки cookie с токеном.
     * @return {@link AuthResponse} с JWT-токеном.
     */
    public AuthResponse login(UserSigninDto dto, HttpServletResponse response) {
        return accountService.login(dto, response);
    }

    /**
     * Подтверждает email пользователя по коду подтверждения.
     *
     * @param email email пользователя.
     * @param code  код подтверждения.
     * @return {@link AuthResponse} с JWT-токеном.
     */
    public AuthResponse confirmEmail(String email, String code) {
        return accountService.confirmEmail(email, code);
    }

    /**
     * Повторно отправляет код подтверждения на указанный email.
     *
     * @param email email пользователя.
     * @throws MessagingException если не удалось отправить письмо.
     */
    public void resendConfirmationCode(String email) throws MessagingException {
        accountService.resendConfirmationCode(email);
    }

    /**
     * Отправляет ссылку на сброс пароля пользователю.
     *
     * @param email email пользователя.
     * @throws MessagingException если не удалось отправить письмо.
     */
    public void sendPasswordResetLink(String email) throws MessagingException {
        accountService.sendPasswordResetLink(email);
    }

    /**
     * Сбрасывает пароль пользователя по токену сброса.
     *
     * @param token       токен для сброса пароля.
     * @param newPassword новый пароль.
     * @return {@link AuthResponse} с JWT-токеном.
     */
    public AuthResponse resetPassword(String token, String newPassword) {
        return accountService.resetPassword(token, newPassword);
    }

    /**
     * Выполняет выход пользователя, удаляя refresh token из Redis и сессию.
     *
     * @param request  HTTP-запрос с cookie.
     * @param response HTTP-ответ для очистки cookie.
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authTokenService.logout(request, response);
    }

    /**
     * Обновляет access JWT-токен на основе refresh token из запроса.
     *
     * @param request  HTTP-запрос с cookie.
     * @param response HTTP-ответ для установки нового refresh token в cookie.
     * @return {@link AuthResponse} с новым access JWT-токеном.
     */
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return authTokenService.refreshToken(request, response);
    }

    /**
     * Добавляет JWT-токен в HttpOnly cookie.
     *
     * @param token    JWT-токен.
     * @param response HTTP-ответ для установки cookie.
     */
    public void addJwtToCookie(String token, HttpServletResponse response) {
        authTokenService.addJwtToCookie(token, response);
    }

    /**
     * Валидирует JWT-токен, проверяет сессию и обновляет её время действия.
     *
     * @param token JWT-токен для проверки.
     */
    public void validateJwtToken(String token) {
        authTokenService.validateJwtToken(token);
    }
}
