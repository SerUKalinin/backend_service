package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.UserSigninDto;
import com.example.auth_service.dto.UserSignupDto;
import com.example.auth_service.exception.*;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.email.EmailService;
import com.example.auth_service.service.redis.RedisService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RedisService redisService;

    @Mock
    private EmailService emailService;

    @Mock
    private SessionService sessionService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthService authService;

    private UserSignupDto userSignupDto;
    private UserSigninDto userSigninDto;
    private User user;
    private Role role;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Инициализация тестовых данных
        userSignupDto = new UserSignupDto();
        userSignupDto.setEmail("test@example.com");
        userSignupDto.setPassword("password123");
        userSignupDto.setUsername("testuser");

        userSigninDto = new UserSigninDto();
        userSigninDto.setUsername("testuser");
        userSigninDto.setPassword("password123");

        role = new Role();
        role.setRoleType(Role.RoleType.ROLE_USER);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setActive(true);
        user.setRoles(Set.of(role));

        authentication = mock(Authentication.class);
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void registerUser_Success() throws MessagingException {
        // Настройка
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleType(any())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doNothing().when(redisService).saveConfirmationCode(anyString(), anyString());

        // Выполнение
        authService.register(userSignupDto, false);

        // Проверка
        verify(userRepository).save(any(User.class));
        verify(emailService).sendConfirmationCode(anyString(), anyString());
        verify(redisService).saveConfirmationCode(anyString(), anyString());
    }

    @Test
    @DisplayName("Регистрация с существующим username")
    void registerUser_UsernameExists() {
        // Настройка
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        // Выполнение и проверка
        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(userSignupDto, false));
    }

    @Test
    @DisplayName("Регистрация с существующим email")
    void registerUser_EmailExists() {
        // Настройка
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Выполнение и проверка
        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(userSignupDto, false));
    }

    @Test
    @DisplayName("Успешный вход в систему")
    void login_Success() {
        // Настройка
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(Set.of());
        when(jwtUtil.generateToken("testuser", Set.of())).thenReturn("jwt-token");

        // Выполнение
        AuthResponse response = authService.login(userSigninDto);

        // Проверка
        assertNotNull(response);
        assertEquals("jwt-token", response.getJwtToken());
        verify(sessionService).saveSession(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Вход с несуществующим username")
    void login_UserNotFound() {
        // Настройка
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Выполнение и проверка
        assertThrows(UserNotFoundException.class,
                () -> authService.login(userSigninDto));
    }

    @Test
    @DisplayName("Вход с неактивированным аккаунтом")
    void login_UserNotActivated() {
        // Настройка
        user.setActive(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        // Выполнение и проверка
        assertThrows(UserNotActivatedException.class,
                () -> authService.login(userSigninDto));
    }

    @Test
    @DisplayName("Успешное подтверждение email")
    void confirmEmail_Success() {
        // Настройка
        String email = "test@example.com";
        String code = "123456";
        when(redisService.checkConfirmationCode(email, code)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Выполнение
        authService.confirmEmail(email, code);

        // Проверка
        verify(userRepository).save(user);
        verify(redisService).deleteConfirmationCode(email);
        assertTrue(user.isActive());
    }

    @Test
    @DisplayName("Подтверждение email с неверным кодом")
    void confirmEmail_InvalidCode() {
        // Настройка
        String email = "test@example.com";
        String code = "wrong-code";
        when(redisService.checkConfirmationCode(email, code)).thenReturn(false);

        // Выполнение и проверка
        assertThrows(InvalidConfirmationCodeException.class,
                () -> authService.confirmEmail(email, code));
    }

    @Test
    @DisplayName("Успешный выход из системы")
    void logout_Success() {
        // Настройка
        String token = "valid-token";
        String authHeader = "Bearer " + token;

        // Выполнение
        authService.logout(authHeader);

        // Проверка
        verify(jwtUtil).addJwtToBlacklist(token);
    }

    @Test
    @DisplayName("Выход с неверным форматом токена")
    void logout_InvalidTokenFormat() {
        // Настройка
        String invalidHeader = "InvalidFormat token";

        // Выполнение и проверка
        assertThrows(AuthException.class,
                () -> authService.logout(invalidHeader));
    }

    @Test
    @DisplayName("Успешная повторная отправка кода подтверждения")
    void resendConfirmationCode_Success() throws MessagingException {
        // Настройка
        String email = "test@example.com";
        user.setActive(false); // Устанавливаем пользователя как неактивированного
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(redisService).saveConfirmationCode(anyString(), anyString());

        // Выполнение
        authService.resendConfirmationCode(email);

        // Проверка
        verify(emailService).sendConfirmationCode(anyString(), anyString());
        verify(redisService).saveConfirmationCode(anyString(), anyString());
    }

    @Test
    @DisplayName("Повторная отправка кода для несуществующего email")
    void resendConfirmationCode_UserNotFound() {
        // Настройка
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Выполнение и проверка
        assertThrows(UserNotFoundException.class,
                () -> authService.resendConfirmationCode(email));
    }

    @Test
    @DisplayName("Повторная отправка кода для уже активированного пользователя")
    void resendConfirmationCode_UserAlreadyActivated() {
        // Настройка
        String email = "test@example.com";
        user.setActive(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Выполнение и проверка
        assertThrows(UserAlreadyExistsException.class,
                () -> authService.resendConfirmationCode(email));
    }
} 