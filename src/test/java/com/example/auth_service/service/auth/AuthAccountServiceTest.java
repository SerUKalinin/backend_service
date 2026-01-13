package com.example.auth_service.service.auth;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.UserSigninDto;
import com.example.auth_service.dto.UserSignupDto;
import com.example.auth_service.exception.InvalidConfirmationCodeException;
import com.example.auth_service.exception.UserAlreadyExistsException;
import com.example.auth_service.exception.UserNotActivatedException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.SessionService;
import com.example.auth_service.service.email.EmailService;
import com.example.auth_service.service.redis.RedisService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthAccountServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuthenticationManager authenticationManager;
    private RedisService redisService;
    private EmailService emailService;
    private SessionService sessionService;

    private AuthAccountService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtil = mock(JwtUtil.class);
        authenticationManager = mock(AuthenticationManager.class);
        redisService = mock(RedisService.class);
        emailService = mock(EmailService.class);
        sessionService = mock(SessionService.class);

        authService = new AuthAccountService(
                userRepository,
                roleRepository,
                passwordEncoder,
                jwtUtil,
                authenticationManager,
                redisService,
                emailService,
                sessionService
        );
    }

    @Test
    void register_shouldSaveUserAndSendCode() throws MessagingException {
        UserSignupDto dto = new UserSignupDto();
        dto.setUsername("user1");
        dto.setEmail("user1@mail.com");
        dto.setPassword("password123");
        dto.setFirstName("Sergey");
        dto.setLastName("Kalinin");

        Role role = new Role();
        role.setRoleType(Role.RoleType.ROLE_USER);

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleType(Role.RoleType.ROLE_USER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPass");

        authService.register(dto, false);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("user1", savedUser.getUsername());
        assertEquals("user1@mail.com", savedUser.getEmail());
        assertEquals("encodedPass", savedUser.getPassword());
        assertFalse(savedUser.isActive());
        assertTrue(savedUser.getRoles().contains(role));

        verify(redisService).saveConfirmationCode(eq("user1@mail.com"), anyString());
        verify(emailService).sendConfirmationCode(eq("user1@mail.com"), anyString());
    }

    @Test
    void login_shouldReturnAuthResponse_whenUserActive() {
        UserSigninDto dto = new UserSigninDto();
        dto.setUsername("user1");
        dto.setPassword("password123");

        User user = new User();
        user.setUsername("user1");
        user.setActive(true);
        Role role = new Role();
        role.setRoleType(Role.RoleType.ROLE_USER);
        user.setRoles(Set.of(role));

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(User.class), anyCollection())).thenReturn("jwtToken");

        AuthResponse response = authService.login(dto, mock(HttpServletResponse.class));

        assertEquals("jwtToken", response.getJwtToken()); // <- исправлено
        verify(sessionService).saveSession(eq("user1"), eq("jwtToken"), any(Duration.class));
    }

    @Test
    void login_shouldThrow_whenUserNotActive() {
        UserSigninDto dto = new UserSigninDto();
        dto.setUsername("user1");
        dto.setPassword("password123");

        User user = new User();
        user.setUsername("user1");
        user.setActive(false);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        assertThrows(UserNotActivatedException.class, () -> authService.login(dto, mock(HttpServletResponse.class)));
    }

    @Test
    void confirmEmail_shouldActivateUser() {
        String email = "user@mail.com";
        String code = "123456";
        User user = new User();
        user.setEmail(email);
        user.setUsername("user1");
        user.setActive(false);
        Role role = new Role();
        role.setRoleType(Role.RoleType.ROLE_USER);
        user.setRoles(Set.of(role));

        when(redisService.checkConfirmationCode(email, code)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(User.class), anyCollection())).thenReturn("jwtToken");

        AuthResponse response = authService.confirmEmail(email, code);

        assertTrue(user.isActive());
        assertEquals("jwtToken", response.getJwtToken()); // <- исправлено
        verify(redisService).deleteConfirmationCode(email);
        verify(sessionService).saveSession(eq("user1"), eq("jwtToken"), any(Duration.class));
    }

    @Test
    void confirmEmail_shouldThrowIfCodeInvalid() {
        when(redisService.checkConfirmationCode("email", "code")).thenReturn(false);
        assertThrows(InvalidConfirmationCodeException.class, () -> authService.confirmEmail("email", "code"));
    }

    @Test
    void resendConfirmationCode_shouldThrowIfUserActive() throws MessagingException {
        User user = new User();
        user.setActive(true);
        when(userRepository.findByEmail("email")).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> authService.resendConfirmationCode("email"));
    }

    @Test
    void sendPasswordResetLink_shouldGenerateTokenAndSendEmail() throws MessagingException {
        User user = new User();
        user.setEmail("email");
        user.setUsername("user1");

        when(userRepository.findByEmail("email")).thenReturn(Optional.of(user));
        when(jwtUtil.generatePasswordResetToken("user1")).thenReturn("resetToken");

        authService.sendPasswordResetLink("email");

        verify(redisService).savePasswordResetToken("email", "resetToken", Duration.ofHours(1));
        verify(emailService).sendPasswordResetEmail("email", "http://localhost:3000/reset-password?token=resetToken");
    }

    @Test
    void resetPassword_shouldEncodeAndSaveNewPassword() {
        String token = "resetToken";
        String newPassword = "newPass";

        User user = new User();
        user.setUsername("user1");
        user.setEmail("email");

        // Обязательно добавляем роли
        Role role = new Role();
        role.setRoleType(Role.RoleType.ROLE_USER);
        user.setRoles(Set.of(role));

        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(decodedJWT.getSubject()).thenReturn("user1");
        when(jwtUtil.decodePasswordResetToken(token)).thenReturn(decodedJWT);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPass");
        when(jwtUtil.generateToken(any(User.class), anyCollection())).thenReturn("jwtToken");

        AuthResponse response = authService.resetPassword(token, newPassword);

        assertEquals("jwtToken", response.getJwtToken());
        assertEquals("encodedNewPass", user.getPassword());
        verify(redisService).deletePasswordResetToken("email");
    }
}
