package com.example.auth_service.service.auth;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.UserSigninDto;
import com.example.auth_service.dto.UserSignupDto;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private AuthAccountService accountService;
    private AuthTokenService authTokenService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        accountService = mock(AuthAccountService.class);
        authTokenService = mock(AuthTokenService.class);
        authService = new AuthService(accountService, authTokenService);
    }

    @Test
    @DisplayName("Регистрация пользователя: делегирует вызов AuthAccountService")
    void register_shouldDelegateToAccountService() throws MessagingException {
        UserSignupDto dto = new UserSignupDto();
        boolean isAdmin = false;

        authService.register(dto, isAdmin);

        verify(accountService).register(dto, isAdmin);
    }

    @Test
    @DisplayName("Логин пользователя: делегирует вызов AuthAccountService и возвращает JWT")
    void login_shouldDelegateToAccountService() {
        UserSigninDto dto = new UserSigninDto();
        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthResponse expected = new AuthResponse("jwtToken");
        when(accountService.login(dto, response)).thenReturn(expected);

        AuthResponse actual = authService.login(dto, response);

        assertEquals(expected.getJwtToken(), actual.getJwtToken());
        verify(accountService).login(dto, response);
    }

    @Test
    @DisplayName("Подтверждение email: делегирует вызов AuthAccountService и возвращает JWT")
    void confirmEmail_shouldDelegateToAccountService() {
        String email = "test@mail.com";
        String code = "123456";
        AuthResponse expected = new AuthResponse("jwtToken");
        when(accountService.confirmEmail(email, code)).thenReturn(expected);

        AuthResponse actual = authService.confirmEmail(email, code);

        assertEquals(expected.getJwtToken(), actual.getJwtToken());
        verify(accountService).confirmEmail(email, code);
    }

    @Test
    @DisplayName("Повторная отправка кода подтверждения: делегирует вызов AuthAccountService")
    void resendConfirmationCode_shouldDelegateToAccountService() throws MessagingException {
        String email = "test@mail.com";

        authService.resendConfirmationCode(email);

        verify(accountService).resendConfirmationCode(email);
    }

    @Test
    @DisplayName("Отправка ссылки для сброса пароля: делегирует вызов AuthAccountService")
    void sendPasswordResetLink_shouldDelegateToAccountService() throws MessagingException {
        String email = "test@mail.com";

        authService.sendPasswordResetLink(email);

        verify(accountService).sendPasswordResetLink(email);
    }

    @Test
    @DisplayName("Сброс пароля: делегирует вызов AuthAccountService и возвращает JWT")
    void resetPassword_shouldDelegateToAccountService() {
        String token = "token";
        String newPassword = "newPass";
        AuthResponse expected = new AuthResponse("jwtToken");

        when(accountService.resetPassword(token, newPassword)).thenReturn(expected);

        AuthResponse actual = authService.resetPassword(token, newPassword);

        assertEquals(expected.getJwtToken(), actual.getJwtToken());
        verify(accountService).resetPassword(token, newPassword);
    }

    @Test
    @DisplayName("Выход пользователя: делегирует вызов AuthTokenService")
    void logout_shouldDelegateToAuthTokenService() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        authService.logout(request, response);

        verify(authTokenService).logout(request, response);
    }

    @Test
    @DisplayName("Обновление токена: делегирует вызов AuthTokenService и возвращает JWT")
    void refreshToken_shouldDelegateToAuthTokenService() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthResponse expected = new AuthResponse("newJwt");
        when(authTokenService.refreshToken(request, response)).thenReturn(expected);

        AuthResponse actual = authService.refreshToken(request, response);

        assertEquals(expected.getJwtToken(), actual.getJwtToken());
        verify(authTokenService).refreshToken(request, response);
    }

    @Test
    @DisplayName("Добавление JWT в cookie: делегирует вызов AuthTokenService")
    void addJwtToCookie_shouldDelegateToAuthTokenService() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        String token = "jwtToken";

        authService.addJwtToCookie(token, response);

        verify(authTokenService).addJwtToCookie(token, response);
    }

    @Test
    @DisplayName("Валидация JWT: делегирует вызов AuthTokenService")
    void validateJwtToken_shouldDelegateToAuthTokenService() {
        String token = "jwtToken";

        authService.validateJwtToken(token);

        verify(authTokenService).validateJwtToken(token);
    }
}
