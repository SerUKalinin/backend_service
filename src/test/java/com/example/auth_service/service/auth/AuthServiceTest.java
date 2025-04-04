//package com.example.auth_service.service.auth;
//
//import com.example.auth_service.dto.AuthResponse;
//import com.example.auth_service.dto.UserSigninDto;
//import com.example.auth_service.dto.UserSignupDto;
//import com.example.auth_service.exception.*;
//import com.example.auth_service.model.Role;
//import com.example.auth_service.model.User;
//import com.example.auth_service.repository.RoleRepository;
//import com.example.auth_service.repository.UserRepository;
//import com.example.auth_service.service.*;
//import com.example.auth_service.service.email.EmailService;
//import com.example.auth_service.service.redis.RedisService;
//import com.example.auth_service.service.security.jwt.JwtUtil;
//import jakarta.mail.MessagingException;
//import org.junit.jupiter.api.*;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.authentication.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class AuthServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private RoleRepository roleRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private JwtUtil jwtUtil;
//
//    @Mock
//    private AuthenticationManager authenticationManager;
//
//    @Mock
//    private RedisService redisService;
//
//    @Mock
//    private EmailService emailService;
//
//    @Mock
//    private SessionService sessionService;
//
//    @InjectMocks
//    private AuthService authService;
//
//    // =======================
//    // Тесты регистрации
//    // =======================
//
//    @Test
//    @DisplayName("register - Успешная регистрация пользователя")
//    void registerUser_Success() throws MessagingException {
//        UserSignupDto userSignupDto = new UserSignupDto("com/example/auth_service/service/user", "user@example.com", "password");
//        Role userRole = new Role();
//        userRole.setRoleType(Role.RoleType.ROLE_USER);
//
//        when(userRepository.findByUsername("com/example/auth_service/service/user")).thenReturn(Optional.empty());
//        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
//        when(roleRepository.findByRoleType(Role.RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
//        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
//
//        assertDoesNotThrow(() -> authService.register(userSignupDto, false));
//        verify(emailService, times(1)).sendConfirmationCode(any(), any());
//    }
//
//    @Test
//    @DisplayName("register - Ошибка: пользователь уже существует")
//    void register_UserAlreadyExists() {
//        UserSignupDto userSignupDto = new UserSignupDto("com/example/auth_service/service/user", "user@example.com", "password");
//        when(userRepository.findByUsername("com/example/auth_service/service/user")).thenReturn(Optional.of(new User()));
//
//        assertThrows(UserAlreadyExistsException.class,
//                () -> authService.register(userSignupDto, false));
//    }
//
//    @Test
//    @DisplayName("register - Ошибка: email уже зарегистрирован")
//    void register_EmailAlreadyExists() {
//        UserSignupDto userSignupDto = new UserSignupDto("com/example/auth_service/service/user", "user@example.com", "password");
//        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new User()));
//
//        assertThrows(EmailAlreadyExistsException.class,
//                () -> authService.register(userSignupDto, false));
//    }
//
//    @Test
//    @DisplayName("register - Ошибка: роль пользователя не найдена")
//    void register_RoleNotFound() {
//        UserSignupDto userSignupDto = new UserSignupDto("com/example/auth_service/service/user", "user@example.com", "password");
//
//        when(userRepository.findByUsername("com/example/auth_service/service/user")).thenReturn(Optional.empty());
//        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
//        when(roleRepository.findByRoleType(Role.RoleType.ROLE_USER)).thenReturn(Optional.empty());
//
//        assertThrows(IllegalStateException.class,
//                () -> authService.register(userSignupDto, false));
//    }
//
//    // =======================
//    // Тесты входа
//    // =======================
//
//    @Test
//    @DisplayName("login - Успешная аутентификация")
//    void login_Success() {
//        UserSigninDto userSigninDto = new UserSigninDto("com/example/auth_service/service/user", "password");
//        User user = new User();
//        user.setUsername("com/example/auth_service/service/user");
//        user.setPassword("encoded-password");
//        user.setActive(true);
//
//        Authentication auth = new UsernamePasswordAuthenticationToken(
//                "com/example/auth_service/service/user", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//
//        when(userRepository.findByUsername("com/example/auth_service/service/user")).thenReturn(Optional.of(user));
//        when(authenticationManager.authenticate(any())).thenReturn(auth);
//        when(jwtUtil.generateToken(any(), any())).thenReturn("jwt-token");
//
//        // Мокируем вызов метода saveSession
//        doNothing().when(sessionService).saveSession(any(), any(), any());
//
//        AuthResponse response = authService.login(userSigninDto);
//
//        assertNotNull(response);
//        assertEquals("jwt-token", response.getJwtToken());
//        verify(sessionService, times(1)).saveSession(any(), any(), any());  // Проверка вызова метода
//    }
//
//    @Test
//    @DisplayName("login - Ошибка: пользователь не найден")
//    void login_UserNotFound() {
//        UserSigninDto userSigninDto = new UserSigninDto("com/example/auth_service/service/user", "password");
//
//        when(userRepository.findByUsername("com/example/auth_service/service/user")).thenReturn(Optional.empty());
//
//        assertThrows(UserNotFoundException.class,
//                () -> authService.login(userSigninDto));
//    }
//
//    @Test
//    @DisplayName("login - Ошибка: пользователь не активирован")
//    void login_UserNotActivated() {
//        UserSigninDto userSigninDto = new UserSigninDto("com/example/auth_service/service/user", "password");
//        User user = new User();
//        user.setUsername("com/example/auth_service/service/user");
//        user.setActive(false);
//
//        when(userRepository.findByUsername("com/example/auth_service/service/user")).thenReturn(Optional.of(user));
//
//        assertThrows(UserNotActivatedException.class,
//                () -> authService.login(userSigninDto));
//    }
//
//    @Test
//    @DisplayName("login - Ошибка: неверный пароль")
//    void login_IncorrectPassword() {
//        UserSigninDto userSigninDto = new UserSigninDto("com/example/auth_service/service/user", "wrong-password");
//        User user = new User();
//        user.setUsername("com/example/auth_service/service/user");
//        user.setPassword("encoded-password");
//        user.setActive(true);
//
//        when(userRepository.findByUsername("com/example/auth_service/service/user")).thenReturn(Optional.of(user));
//        when(authenticationManager.authenticate(any()))
//                .thenThrow(new BadCredentialsException("Bad credentials"));
//
//        assertThrows(BadCredentialsException.class,
//                () -> authService.login(userSigninDto));
//    }
//
//    // =======================
//    // Тесты для подтверждения почты
//    // =======================
//
//    @Test
//    @DisplayName("confirmEmail - Успешное подтверждение email")
//    void confirmEmail_Success() {
//        String email = "user@example.com";
//        String code = "123456";
//        User user = new User();
//        user.setEmail(email);
//        user.setActive(false);
//
//        when(redisService.checkConfirmationCode(email, code)).thenReturn(true);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//
//        assertDoesNotThrow(() -> authService.confirmEmail(email, code));
//
//        assertTrue(user.isActive());
//        verify(redisService, times(1)).deleteConfirmationCode(email);
//    }
//
//    @Test
//    @DisplayName("confirmEmail - Ошибка: неверный код подтверждения")
//    void confirmEmail_InvalidCode() {
//        String email = "user@example.com";
//        String code = "wrong-code";
//
//        when(redisService.checkConfirmationCode(email, code)).thenReturn(false);
//
//        assertThrows(InvalidConfirmationCodeException.class,
//                () -> authService.confirmEmail(email, code));
//    }
//
//    @Test
//    @DisplayName("confirmEmail - Ошибка: email уже подтвержден")
//    void confirmEmail_AlreadyConfirmed() {
//        String email = "user@example.com";
//        String code = "123456";
//        User user = new User();
//        user.setEmail(email);
//        user.setActive(true);
//
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//
//        assertThrows(InvalidConfirmationCodeException.class,
//                () -> authService.confirmEmail(email, code));
//    }
//
//    // =======================
//    // Тест выхода
//    // =======================
//
//    @Test
//    @DisplayName("logout - Успешный выход из системы")
//    void logout_Success() {
//        String authHeader = "Bearer jwt-token";
//
//        assertDoesNotThrow(() -> authService.logout(authHeader));
//        verify(jwtUtil, times(1)).addJwtToBlacklist("jwt-token");
//    }
//}
