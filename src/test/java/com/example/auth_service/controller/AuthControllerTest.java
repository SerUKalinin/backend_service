package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.service.SessionService;
import com.example.auth_service.service.auth.AuthService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthService authService;
    private JwtUtil jwtUtil;
    private SessionService sessionService;
    private AuthController authController;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        jwtUtil = mock(JwtUtil.class);
        sessionService = mock(SessionService.class);
        authController = new AuthController(authService, jwtUtil, sessionService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    void register_shouldCallAuthServiceRegister() throws MessagingException {
        UserSignupDto dto = new UserSignupDto();
        dto.setEmail("user@example.com");
        dto.setPassword("password");

        authController.register(dto);

        verify(authService).register(dto, false);
    }

    @Test
    void registerAdmin_shouldCallAuthServiceRegisterWithAdminFlag() throws MessagingException {
        UserSignupDto dto = new UserSignupDto();
        dto.setEmail("admin@example.com");
        dto.setPassword("password");

        authController.registerAdmin(dto);

        verify(authService).register(dto, true);
    }

    @Test
    void login_shouldReturnAuthResponse() {
        UserSigninDto dto = new UserSigninDto();
        dto.setUsername("user");
        dto.setPassword("password");

        AuthResponse authResponse = new AuthResponse("dummy-token");
        when(authService.login(dto, response)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> responseEntity = authController.login(dto, response);

        assertNotNull(responseEntity);
        assertEquals("dummy-token", responseEntity.getBody().getJwtToken());
        verify(authService).login(dto, response);
    }

    @Test
    void logout_shouldCallAuthServiceLogout() {
        authController.logout(request, response);

        verify(authService).logout(request, response);
    }

    @Test
    void verifyEmail_shouldReturnAuthResponseAndCallAddJwtToCookie() {
        EmailVerificationDto dto = new EmailVerificationDto();
        dto.setEmail("user@example.com");
        dto.setCode("123456");

        AuthResponse authResponse = new AuthResponse("dummy-token");
        when(authService.confirmEmail(dto.getEmail(), dto.getCode())).thenReturn(authResponse);

        ResponseEntity<AuthResponse> responseEntity = authController.verifyEmail(dto, response);

        assertNotNull(responseEntity);
        assertEquals("dummy-token", responseEntity.getBody().getJwtToken());
        verify(authService).confirmEmail(dto.getEmail(), dto.getCode());
        verify(authService).addJwtToCookie("dummy-token", response);
    }

    @Test
    void resendEmailVerification_shouldCallAuthServiceResendConfirmationCode() throws MessagingException {
        String email = "user@example.com";

        authController.resendEmailVerification(email);

        verify(authService).resendConfirmationCode(email);
    }

    @Test
    void refreshToken_shouldReturnAuthResponse() {
        AuthResponse authResponse = new AuthResponse("dummy-token");
        when(authService.refreshToken(request, response)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> responseEntity = authController.refreshToken(request, response);

        assertNotNull(responseEntity);
        assertEquals("dummy-token", responseEntity.getBody().getJwtToken());
        verify(authService).refreshToken(request, response);
    }

    @Test
    void forgotPassword_shouldCallAuthServiceSendPasswordResetLink() throws MessagingException {
        ForgotPasswordDto dto = new ForgotPasswordDto();
        dto.setEmail("user@example.com");

        authController.forgotPassword(dto);

        verify(authService).sendPasswordResetLink(dto.getEmail());
    }

    @Test
    void resetPassword_shouldReturnAuthResponse() {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken("token");
        dto.setNewPassword("newPassword");

        AuthResponse authResponse = new AuthResponse("dummy-token");
        when(authService.resetPassword(dto.getToken(), dto.getNewPassword())).thenReturn(authResponse);

        ResponseEntity<AuthResponse> responseEntity = authController.resetPassword(dto);

        assertNotNull(responseEntity);
        assertEquals("dummy-token", responseEntity.getBody().getJwtToken());
        verify(authService).resetPassword(dto.getToken(), dto.getNewPassword());
    }

    @Test
    void validateToken_shouldReturnOkWhenHeaderIsValid() {
        String token = "valid-token";
        String header = "Bearer " + token;

        ResponseEntity<Void> responseEntity = authController.validateToken(header);

        assertEquals(200, responseEntity.getStatusCodeValue());
        verify(authService).validateJwtToken(token);
    }

    @Test
    void validateToken_shouldReturnUnauthorizedWhenHeaderIsInvalid() {
        ResponseEntity<Void> responseEntity = authController.validateToken(null);

        assertEquals(401, responseEntity.getStatusCodeValue());
        verify(authService, never()).validateJwtToken(any());
    }
}
