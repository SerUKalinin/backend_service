package com.example.auth_service.service.auth;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис для управления аккаунтами пользователей.
 *
 * <p>Отвечает за регистрацию, аутентификацию, подтверждение email, повторную отправку кодов,
 * сброс пароля и генерацию JWT токенов. Обеспечивает интеграцию с {@link UserRepository}, {@link RoleRepository},
 * {@link RedisService}, {@link EmailService} и {@link SessionService}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthAccountService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CONFIRMATION_CODE_MIN = 100_000;
    private static final int CONFIRMATION_CODE_MAX = 999_999;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RedisService redisService;
    private final EmailService emailService;
    private final SessionService sessionService;

    /**
     * Регистрирует нового пользователя или администратора.
     *
     * @param dto     DTO с данными для регистрации
     * @param isAdmin true, если регистрируется администратор
     * @throws MessagingException           если не удалось отправить email с кодом подтверждения
     * @throws UserAlreadyExistsException   если username или email уже заняты
     */
    @Transactional
    public void register(UserSignupDto dto, boolean isAdmin) throws MessagingException {
        validateUserUniqueness(dto);

        User user = createUser(dto, isAdmin);
        userRepository.save(user);

        sendConfirmationCode(user.getEmail());
    }

    /**
     * Аутентифицирует пользователя по логину и паролю, создаёт сессию и сохраняет refresh-токен.
     *
     * @param dto      DTO с данными для входа
     * @param response HTTP-ответ для установки cookie (не используется в текущей версии)
     * @return DTO с JWT-токеном доступа
     * @throws UserNotActivatedException если аккаунт не активирован
     * @throws UserNotFoundException     если пользователь не найден
     */
    @Transactional
    public AuthResponse login(UserSigninDto dto, HttpServletResponse response) {
        User user = findUser(dto.getUsername());

        if (!user.isActive()) {
            throw new UserNotActivatedException("Аккаунт не активирован");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), dto.getPassword())
        );

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken();

        sessionService.saveSession(user.getUsername(), accessToken, Duration.ofHours(2));
        redisService.saveRefreshToken(user.getUsername(), refreshToken, Duration.ofDays(7));

        return new AuthResponse(accessToken);
    }

    /**
     * Подтверждает email пользователя по коду.
     *
     * @param email email пользователя
     * @param code  код подтверждения
     * @return DTO с JWT-токеном
     * @throws InvalidConfirmationCodeException если код неверный или истёк
     * @throws UserNotFoundException            если пользователь не найден
     */
    @Transactional
    public AuthResponse confirmEmail(String email, String code) {
        if (!redisService.checkConfirmationCode(email, code)) {
            throw new InvalidConfirmationCodeException("Неверный или истекший код");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setActive(true);
        userRepository.save(user);
        redisService.deleteConfirmationCode(email);

        String token = generateAccessToken(user);
        sessionService.saveSession(user.getUsername(), token, Duration.ofHours(2));

        return new AuthResponse(token);
    }

    /**
     * Повторно отправляет код подтверждения на email пользователя.
     *
     * @param email email пользователя
     * @throws MessagingException           если не удалось отправить email
     * @throws UserNotFoundException        если пользователь не найден
     * @throws UserAlreadyExistsException   если пользователь уже активирован
     */
    public void resendConfirmationCode(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (user.isActive()) {
            throw new UserAlreadyExistsException("Пользователь уже активирован");
        }

        sendConfirmationCode(email);
    }

    /**
     * Отправляет ссылку для сброса пароля на email пользователя.
     *
     * @param email email пользователя
     * @throws MessagingException    если не удалось отправить письмо
     * @throws UserNotFoundException если пользователь не найден
     */
    public void sendPasswordResetLink(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        String token = jwtUtil.generatePasswordResetToken(user.getUsername());
        redisService.savePasswordResetToken(email, token, Duration.ofHours(1));

        emailService.sendPasswordResetEmail(
                email,
                "http://localhost:3000/reset-password?token=" + token
        );
    }

    /**
     * Сбрасывает пароль пользователя по токену из письма.
     *
     * @param token       JWT-токен сброса пароля
     * @param newPassword новый пароль пользователя
     * @return DTO с JWT-токеном доступа
     * @throws UserNotFoundException если пользователь не найден
     * @throws RuntimeException      если токен недействителен
     */
    @Transactional
    public AuthResponse resetPassword(String token, String newPassword) {
        DecodedJWT jwt = decodeResetToken(token);

        User user = userRepository.findByUsername(jwt.getSubject())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redisService.deletePasswordResetToken(user.getEmail());

        return new AuthResponse(generateAccessToken(user));
    }

    /* ---------------------- PRIVATE METHODS ---------------------- */

    /**
     * Проверяет уникальность username и email.
     *
     * @param dto DTO с данными пользователя
     * @throws UserAlreadyExistsException если username или email уже заняты
     */
    private void validateUserUniqueness(UserSignupDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("Username занят");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email занят");
        }
    }

    /**
     * Находит пользователя по username или email.
     *
     * @param usernameOrEmail username или email
     * @return найденный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    private User findUser(String usernameOrEmail) {
        return usernameOrEmail.contains("@")
                ? userRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"))
                : userRepository.findByUsername(usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    /**
     * Создаёт сущность пользователя с указанной ролью.
     *
     * @param dto     DTO с данными для регистрации
     * @param isAdmin true, если создаётся администратор
     * @return новая сущность {@link User}
     * @throws IllegalStateException если роль не найдена в базе
     */
    private User createUser(UserSignupDto dto, boolean isAdmin) {
        Role role = roleRepository.findByRoleType(
                isAdmin ? Role.RoleType.ROLE_ADMIN : Role.RoleType.ROLE_USER
        ).orElseThrow(() -> new IllegalStateException("Роль не найдена"));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setActive(false);
        user.setRoles(Set.of(role));
        return user;
    }

    /**
     * Генерирует код подтверждения и отправляет его на email.
     *
     * @param email email пользователя
     * @throws MessagingException если не удалось отправить письмо
     */
    private void sendConfirmationCode(String email) throws MessagingException {
        String code = String.valueOf(
                SECURE_RANDOM.nextInt(CONFIRMATION_CODE_MAX - CONFIRMATION_CODE_MIN + 1)
                        + CONFIRMATION_CODE_MIN
        );
        redisService.saveConfirmationCode(email, code);
        emailService.sendConfirmationCode(email, code);
    }

    /**
     * Декодирует JWT-токен сброса пароля.
     *
     * @param token JWT-токен сброса пароля
     * @return {@link DecodedJWT}
     * @throws RuntimeException если токен недействителен
     */
    private DecodedJWT decodeResetToken(String token) {
        try {
            return jwtUtil.decodePasswordResetToken(token);
        } catch (Exception e) {
            throw new RuntimeException("Токен сброса пароля недействителен");
        }
    }

    /**
     * Генерирует JWT-токен доступа для пользователя.
     *
     * @param user пользователь
     * @return JWT-токен доступа
     */
    private String generateAccessToken(User user) {
        return jwtUtil.generateToken(
                user,
                user.getRoles().stream()
                        .map(r -> r.getRoleType().name())
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }

    /**
     * Генерирует случайный refresh-токен.
     *
     * @return refresh-токен
     */
    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }
}
