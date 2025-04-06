package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.SessionService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SessionService sessionService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private Validator validator;

    private UserSignupDto userSignupDto;
    private UserSigninDto userSigninDto;
    private EmailVerificationDto emailVerificationDto;
    private ForgotPasswordDto forgotPasswordDto;
    private PasswordResetDto passwordResetDto;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        userSignupDto = new UserSignupDto();
        userSignupDto.setEmail("test@example.com");
        userSignupDto.setPassword("password123");
        userSignupDto.setUsername("testuser");

        userSigninDto = new UserSigninDto();
        userSigninDto.setUsername("testuser");
        userSigninDto.setPassword("password123");

        emailVerificationDto = new EmailVerificationDto();
        emailVerificationDto.setEmail("test@example.com");
        emailVerificationDto.setCode("123456");

        forgotPasswordDto = new ForgotPasswordDto();
        forgotPasswordDto.setEmail("test@example.com");

        passwordResetDto = new PasswordResetDto();
        passwordResetDto.setToken("reset-token");
        passwordResetDto.setNewPassword("newPassword123");

        authResponse = new AuthResponse("jwt-token");
    }

    @Test
    @DisplayName("Регистрация с невалидным email")
    void registerUser_InvalidEmail() throws Exception {
        userSignupDto.setEmail("invalid-email");

        mockMvc.perform(post("/auth/register-user")
                .contentType("application/json")
                .content("{\"email\":\"invalid-email\",\"password\":\"password123\",\"username\":\"testuser\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Регистрация с пустым паролем")
    void registerUser_EmptyPassword() throws Exception {
        userSignupDto.setPassword("");

        mockMvc.perform(post("/auth/register-user")
                .contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"\",\"username\":\"testuser\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Регистрация с коротким паролем")
    void registerUser_ShortPassword() throws Exception {
        userSignupDto.setPassword("123");

        mockMvc.perform(post("/auth/register-user")
                .contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"123\",\"username\":\"testuser\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Регистрация с пустым username")
    void registerUser_EmptyUsername() throws Exception {
        userSignupDto.setUsername("");

        mockMvc.perform(post("/auth/register-user")
                .contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\",\"username\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Регистрация с null email")
    void registerUser_NullEmail() throws Exception {
        userSignupDto.setEmail(null);

        mockMvc.perform(post("/auth/register-user")
                .contentType("application/json")
                .content("{\"email\":null,\"password\":\"password123\",\"username\":\"testuser\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void registerUser_Success() throws MessagingException {
        authController.register(userSignupDto);

        verify(authService).register(userSignupDto, false);
    }

    @Test
    @DisplayName("Успешная регистрация администратора")
    void registerAdmin_Success() throws MessagingException {
        authController.registerAdmin(userSignupDto);

        verify(authService).register(userSignupDto, true);
    }

    @Test
    @DisplayName("Успешный вход в систему")
    void login_Success() throws MessagingException {
        when(authService.login(any(UserSigninDto.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(userSigninDto, this.response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        verify(authService).addJwtToCookie("jwt-token", this.response);
    }

    @Test
    @DisplayName("Успешный выход из системы")
    void logout_Success() {
        authController.logout("Bearer token");

        verify(authService).logout("Bearer token");
    }

    @Test
    @DisplayName("Успешная верификация email")
    void verifyEmail_Success() {
        authController.verifyEmail(emailVerificationDto);

        verify(authService).confirmEmail(emailVerificationDto.getEmail(), emailVerificationDto.getCode());
    }

    @Test
    @DisplayName("Успешная повторная отправка кода верификации")
    void resendEmailVerification_Success() throws MessagingException {
        authController.resendEmailVerification("test@example.com");

        verify(authService).resendConfirmationCode("test@example.com");
    }

    @Test
    @DisplayName("Успешное обновление токена")
    void refreshToken_Success() {
        String token = "valid-token";
        String authHeader = "Bearer " + token;
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        
        when(jwtUtil.decodeToken(token)).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("testuser");
        when(sessionService.isSessionValid("testuser", token)).thenReturn(true);
        doNothing().when(sessionService).refreshSession(anyString(), any(Duration.class));

        ResponseEntity<AuthResponse> response = authController.refreshToken(authHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(token, response.getBody().getJwtToken());
        verify(sessionService).refreshSession("testuser", Duration.ofHours(2));
    }

    @Test
    @DisplayName("Обновление токена с невалидным токеном")
    void refreshToken_InvalidToken() {
        String invalidToken = "invalid-token";
        String authHeader = "Bearer " + invalidToken;
        
        when(jwtUtil.decodeToken(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

        ResponseEntity<AuthResponse> response = authController.refreshToken(authHeader);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Успешный запрос сброса пароля")
    void forgotPassword_Success() throws MessagingException {
        authController.forgotPassword(forgotPasswordDto);

        verify(authService).sendPasswordResetLink(forgotPasswordDto.getEmail());
    }

    @Test
    @DisplayName("Успешный сброс пароля")
    void resetPassword_Success() {
        authController.resetPassword(passwordResetDto);

        verify(authService).resetPassword(passwordResetDto.getToken(), passwordResetDto.getNewPassword());
    }

    @Test
    @DisplayName("Регистрация с существующим email")
    void registerUser_EmailAlreadyExists() throws MessagingException {
        doThrow(new com.example.auth_service.exception.UserAlreadyExistsException("Email already exists"))
                .when(authService).register(any(UserSignupDto.class), anyBoolean());

        assertThrows(com.example.auth_service.exception.UserAlreadyExistsException.class,
                () -> authController.register(userSignupDto));
    }

    @Test
    @DisplayName("Вход с неверными учетными данными")
    void login_InvalidCredentials() throws MessagingException {
        when(authService.login(any(UserSigninDto.class)))
                .thenThrow(new com.example.auth_service.exception.AuthException("Invalid credentials"));

        assertThrows(com.example.auth_service.exception.AuthException.class,
                () -> authController.login(userSigninDto, response));
    }

    @Test
    @DisplayName("Верификация email с неверным кодом")
    void verifyEmail_InvalidCode() {
        doThrow(new com.example.auth_service.exception.InvalidConfirmationCodeException("Invalid code"))
                .when(authService).confirmEmail(anyString(), anyString());

        assertThrows(com.example.auth_service.exception.InvalidConfirmationCodeException.class,
                () -> authController.verifyEmail(emailVerificationDto));
    }

    @Test
    @DisplayName("Обновление токена с пустым токеном")
    void refreshToken_EmptyToken() {
        ResponseEntity<AuthResponse> response = authController.refreshToken("");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Обновление токена с некорректным форматом")
    void refreshToken_InvalidFormat() {
        String invalidHeader = "InvalidFormat token";

        ResponseEntity<AuthResponse> response = authController.refreshToken(invalidHeader);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Обновление токена с невалидной сессией")
    void refreshToken_InvalidSession() {
        String token = "valid-token";
        String authHeader = "Bearer " + token;
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        
        when(jwtUtil.decodeToken(token)).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("testuser");
        when(sessionService.isSessionValid("testuser", token)).thenReturn(false);

        ResponseEntity<AuthResponse> response = authController.refreshToken(authHeader);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Запрос сброса пароля для несуществующего email")
    void forgotPassword_EmailNotFound() throws MessagingException {
        doThrow(new com.example.auth_service.exception.UserNotFoundException("User not found"))
                .when(authService).sendPasswordResetLink(anyString());

        assertThrows(com.example.auth_service.exception.UserNotFoundException.class,
                () -> authController.forgotPassword(forgotPasswordDto));
    }

    @Test
    @DisplayName("Сброс пароля с невалидным токеном")
    void resetPassword_InvalidToken() {
        doThrow(new com.example.auth_service.exception.InvalidDataException("Invalid token"))
                .when(authService).resetPassword(anyString(), anyString());

        assertThrows(com.example.auth_service.exception.InvalidDataException.class,
                () -> authController.resetPassword(passwordResetDto));
    }

    @Test
    @DisplayName("Повторная отправка кода верификации для несуществующего email")
    void resendEmailVerification_EmailNotFound() throws MessagingException {
        doThrow(new com.example.auth_service.exception.UserNotFoundException("User not found"))
                .when(authService).resendConfirmationCode(anyString());

        assertThrows(com.example.auth_service.exception.UserNotFoundException.class,
                () -> authController.resendEmailVerification("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Ошибка отправки email при повторной отправке кода верификации")
    void resendEmailVerification_MessagingException() throws MessagingException {
        doThrow(new MessagingException("Failed to send email"))
                .when(authService).resendConfirmationCode(anyString());

        assertThrows(MessagingException.class,
                () -> authController.resendEmailVerification("test@example.com"));
    }
}
