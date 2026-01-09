package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.UserSigninDto;
import com.example.auth_service.dto.UserSignupDto;
import com.example.auth_service.exception.AuthException;
import com.example.auth_service.exception.EmailAlreadyExistsException;
import com.example.auth_service.exception.InvalidConfirmationCodeException;
import com.example.auth_service.exception.UserAlreadyExistsException;
import com.example.auth_service.exception.UserNotActivatedException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.email.EmailService;
import com.example.auth_service.service.redis.RedisService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис для аутентификации и регистрации пользователей.
 * 
 * <p>Оркестрирует процессы регистрации, входа, подтверждения email, сброса пароля
 * и управления токенами. Делегирует специализированные операции в соответствующие сервисы.</p>
 * 
 * <p><strong>Ответственность:</strong></p>
 * <ul>
 *   <li>Оркестрация бизнес-процессов аутентификации</li>
 *   <li>Координация работы с токенами, сессиями и email</li>
 *   <li>Валидация бизнес-правил на уровне сервиса</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /** Криптографически стойкий генератор случайных чисел для кодов подтверждения. */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /** Минимальное значение для 6-значного кода подтверждения. */
    private static final int CONFIRMATION_CODE_MIN = 100_000;
    
    /** Максимальное значение для 6-значного кода подтверждения. */
    private static final int CONFIRMATION_CODE_MAX = 999_999;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RedisService redisService;
    private final EmailService emailService;
    private final SessionService sessionService;

    @Value("${auth_service.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    /* ==================== Registration ==================== */

    /**
     * Регистрирует нового пользователя или администратора в системе.
     * 
     * <p>Процесс регистрации включает:</p>
     * <ol>
     *   <li>Проверку уникальности username и email</li>
     *   <li>Создание пользователя с неактивным статусом</li>
     *   <li>Генерацию криптографически стойкого кода подтверждения</li>
     *   <li>Отправку кода подтверждения на email пользователя</li>
     * </ol>
     * 
     * <p>Пользователь будет активирован только после подтверждения email через метод
     * {@link #confirmEmail(String, String)}.</p>
     * 
     * @param dto данные для регистрации пользователя (username, email, password)
     * @param isAdmin {@code true} если регистрируется администратор, {@code false} для обычного пользователя
     * @throws UserAlreadyExistsException если пользователь с таким username уже существует
     * @throws EmailAlreadyExistsException если пользователь с таким email уже существует
     * @throws MessagingException если произошла ошибка при отправке email с кодом подтверждения
     * @see #confirmEmail(String, String)
     */
    @Transactional
    public void register(UserSignupDto dto, boolean isAdmin) throws MessagingException {
        log.info("Регистрация {}: {}", isAdmin ? "администратора" : "пользователя", dto.getUsername());

        validateUserUniqueness(dto);

        User user = createUser(dto, isAdmin);
        userRepository.save(user);

        sendConfirmationCode(user.getEmail());

        log.info("Пользователь {} зарегистрирован, код подтверждения отправлен", dto.getUsername());
    }

    /* ==================== Login ==================== */

    /**
     * Выполняет аутентификацию пользователя и выдает токены доступа.
     * 
     * <p>Процесс входа включает:</p>
     * <ol>
     *   <li>Поиск пользователя по username или email</li>
     *   <li>Проверку активности аккаунта (должен быть активирован)</li>
     *   <li>Аутентификацию через Spring Security (проверка пароля)</li>
     *   <li>Генерацию access и refresh токенов</li>
     *   <li>Сохранение токенов в Redis и установку refresh token в cookie</li>
     * </ol>
     * 
     * <p><strong>Security:</strong> Проверка активности выполняется ДО аутентификации
     * для предотвращения лишних операций с неактивными аккаунтами.</p>
     * 
     * @param dto данные для входа (username/email и password)
     * @param response HTTP-ответ для установки cookie с refresh token
     * @return объект {@link AuthResponse} содержащий access token для аутентификации
     * @throws UserNotFoundException если пользователь с указанным username/email не найден
     * @throws UserNotActivatedException если аккаунт пользователя не активирован
     * @throws org.springframework.security.core.AuthenticationException если неверный пароль
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

        persistTokens(user.getUsername(), accessToken, refreshToken, response);

        return new AuthResponse(accessToken);
    }

    /* ==================== Email confirmation ==================== */

    /**
     * Подтверждает email пользователя по коду подтверждения и активирует аккаунт.
     * 
     * <p>После успешного подтверждения:</p>
     * <ul>
     *   <li>Аккаунт пользователя активируется (устанавливается флаг {@code active = true})</li>
     *   <li>Код подтверждения удаляется из Redis</li>
     *   <li>Генерируется JWT токен для автоматического входа</li>
     *   <li>Создается сессия пользователя</li>
     * </ul>
     * 
     * @param email email пользователя, для которого подтверждается аккаунт
     * @param code код подтверждения, отправленный на email
     * @return объект {@link AuthResponse} содержащий JWT токен для автоматического входа
     * @throws InvalidConfirmationCodeException если код неверный, истек или не найден в Redis
     * @throws UserNotFoundException если пользователь с указанным email не найден
     * @see #register(UserSignupDto, boolean)
     * @see #resendConfirmationCode(String)
     */
    @Transactional
    public AuthResponse confirmEmail(String email, String code) {
        if (!redisService.checkConfirmationCode(email, code)) {
            throw new InvalidConfirmationCodeException("Неверный или истекший код");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setActive(true);
        userRepository.save(user); // Сохраняем изменения в БД
        redisService.deleteConfirmationCode(email);

        String token = generateAccessToken(user);
        sessionService.saveSession(user.getUsername(), token, Duration.ofHours(2));

        return new AuthResponse(token);
    }

    /* ==================== Logout ==================== */

    /**
     * Выполняет выход пользователя из системы и инвалидирует все токены.
     * 
     * <p>Процесс выхода включает:</p>
     * <ul>
     *   <li>Извлечение refresh token из cookie запроса</li>
     *   <li>Удаление refresh token из Redis</li>
     *   <li>Удаление всех сессий пользователя</li>
     *   <li>Инвалидацию cookie с refresh token на клиенте</li>
     * </ul>
     * 
     * <p><strong>Security:</strong> Полная инвалидация всех токенов и сессий пользователя
     * обеспечивает безопасный выход из системы.</p>
     * 
     * @param request HTTP-запрос для извлечения refresh token из cookie
     * @param response HTTP-ответ для удаления cookie с refresh token
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken != null) {
            String username = redisService.findUsernameByRefreshToken(refreshToken);
            if (username != null) {
                redisService.deleteRefreshToken(username, refreshToken);
                sessionService.removeSession(username); // финальная инвалидация
            }
        }

        invalidateRefreshCookie(response);
    }

    /**
     * Обновляет access токен на основе валидного refresh токена.
     * 
     * <p>Процесс обновления включает:</p>
     * <ol>
     *   <li>Проверку валидности refresh token в Redis</li>
     *   <li>Генерацию нового access токена</li>
     *   <li>Ротацию refresh token (генерация нового и удаление старого)</li>
     *   <li>Сохранение новых токенов в Redis и cookie</li>
     * </ol>
     * 
     * <p><strong>Security:</strong> Ротация refresh token повышает безопасность,
     * так как старый токен становится недействительным сразу после обновления.</p>
     * 
     * @param refreshToken refresh токен из cookie запроса
     * @param response HTTP-ответ для установки нового refresh token в cookie
     * @return объект {@link AuthResponse} содержащий новый access token
     * @throws AuthException если refresh token недействителен, истек или не найден
     * @throws UserNotFoundException если пользователь, связанный с токеном, не найден
     */
    public AuthResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        String username = redisService.findUsernameByRefreshToken(refreshToken);

        if (username == null || !redisService.isRefreshTokenValid(username, refreshToken)) {
            throw new AuthException("Refresh token недействителен");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken();

        redisService.deleteRefreshToken(username, refreshToken);
        persistTokens(username, newAccessToken, newRefreshToken, response);

        return new AuthResponse(newAccessToken);
    }

    /* ==================== Password reset ==================== */

    /**
     * Отправляет ссылку для сброса пароля на email пользователя.
     * 
     * <p>Процесс включает:</p>
     * <ol>
     *   <li>Поиск пользователя по email</li>
     *   <li>Генерацию JWT токена для сброса пароля</li>
     *   <li>Сохранение токена в Redis с временем жизни 1 час</li>
     *   <li>Отправку email со ссылкой для сброса пароля</li>
     * </ol>
     * 
     * <p>Ссылка содержит токен, который используется в методе
     * {@link #resetPassword(String, String)} для сброса пароля.</p>
     * 
     * @param email email пользователя, для которого запрашивается сброс пароля
     * @throws UserNotFoundException если пользователь с указанным email не найден
     * @throws MessagingException если произошла ошибка при отправке email
     * @see #resetPassword(String, String)
     */
    public void sendPasswordResetLink(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        String token = jwtUtil.generatePasswordResetToken(user.getUsername());
        redisService.savePasswordResetToken(email, token, Duration.ofHours(1));

        emailService.sendPasswordResetEmail(
                email,
                frontendBaseUrl + "/reset-password?token=" + token
        );
    }

    /**
     * Сбрасывает пароль пользователя по токену из email.
     * 
     * <p>Процесс сброса включает:</p>
     * <ol>
     *   <li>Декодирование и валидацию токена сброса пароля</li>
     *   <li>Поиск пользователя по username из токена</li>
     *   <li>Хеширование нового пароля</li>
     *   <li>Сохранение нового пароля в базе данных</li>
     *   <li>Удаление токена сброса из Redis</li>
     *   <li>Генерацию нового JWT токена для автоматического входа</li>
     * </ol>
     * 
     * <p><strong>Security:</strong> Токен сброса пароля действителен только 1 час
     * и может быть использован один раз.</p>
     * 
     * @param token JWT токен для сброса пароля, полученный из email
     * @param newPassword новый пароль пользователя
     * @return объект {@link AuthResponse} содержащий JWT токен для автоматического входа
     * @throws AuthException если токен недействителен, истек или имеет неверный формат
     * @throws UserNotFoundException если пользователь, указанный в токене, не найден
     * @see #sendPasswordResetLink(String)
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

    /* ==================== Helpers ==================== */

    /**
     * Валидирует уникальность username и email перед регистрацией.
     * 
     * @param dto данные регистрации для проверки
     * @throws UserAlreadyExistsException если username уже занят
     * @throws EmailAlreadyExistsException если email уже занят
     */
    private void validateUserUniqueness(UserSignupDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("Username занят");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Email занят");
        }
    }

    /**
     * Находит пользователя по username или email.
     * 
     * <p>Определяет тип входа по наличию символа '@' в строке.
     * Если содержит '@' - ищет по email, иначе - по username.</p>
     * 
     * @param usernameOrEmail username или email пользователя
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
     * Создает нового пользователя с указанной ролью.
     * 
     * <p>Пользователь создается с неактивным статусом и должен быть активирован
     * через подтверждение email.</p>
     * 
     * @param dto данные для регистрации
     * @param isAdmin {@code true} для администратора, {@code false} для обычного пользователя
     * @return созданный пользователь (еще не сохранен в БД)
     * @throws IllegalStateException если роль не найдена в системе
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
     * Генерирует и отправляет код подтверждения на email.
     * 
     * <p>Генерирует криптографически стойкий 6-значный код с помощью
     * {@link SecureRandom} и сохраняет его в Redis для последующей проверки.</p>
     * 
     * @param email email пользователя для отправки кода
     * @throws MessagingException если произошла ошибка при отправке email
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
     * Сохраняет access и refresh токены в Redis и устанавливает refresh token в cookie.
     * 
     * <p>Централизованная логика для устранения дублирования кода в методах
     * {@link #login(UserSigninDto, HttpServletResponse)} и
     * {@link #refreshAccessToken(String, HttpServletResponse)}.</p>
     * 
     * @param username имя пользователя
     * @param access access token для сохранения в сессии
     * @param refresh refresh token для сохранения в Redis и cookie
     * @param response HTTP-ответ для установки cookie
     */
    private void persistTokens(String username, String access, String refresh, HttpServletResponse response) {
        redisService.saveRefreshToken(username, refresh, Duration.ofDays(7));
        sessionService.saveSession(username, access, Duration.ofHours(2));

        Cookie cookie = new Cookie("refreshToken", refresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) Duration.ofDays(7).getSeconds());
        response.addCookie(cookie);
    }

    /**
     * Извлекает refresh token из cookie HTTP-запроса.
     * 
     * @param request HTTP-запрос содержащий cookie
     * @return refresh token из cookie или {@code null}, если cookie не найдена
     */
    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if ("refreshToken".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    /**
     * Инвалидирует cookie с refresh token на клиенте.
     * 
     * <p>Устанавливает cookie с пустым значением и временем жизни 0,
     * что приводит к немедленному удалению cookie в браузере.</p>
     * 
     * @param response HTTP-ответ для установки инвалидированной cookie
     */
    private void invalidateRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Генерирует JWT access token для пользователя.
     * 
     * <p>Централизованная логика генерации токенов для устранения дублирования.
     * Токен содержит информацию о пользователе и его ролях.</p>
     * 
     * @param user пользователь для которого генерируется токен
     * @return JWT access token
     */
    private String generateAccessToken(User user) {
        return jwtUtil.generateToken(
                user,
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getRoleType().name()))
                        .toList()
        );
    }

    /**
     * Генерирует новый refresh token на основе UUID.
     * 
     * @return UUID-based refresh token в виде строки
     */
    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Декодирует и валидирует JWT токен для сброса пароля.
     * 
     * @param token JWT токен для декодирования
     * @return декодированный JWT токен
     * @throws AuthException если токен недействителен, истек или имеет неверный формат
     */
    private DecodedJWT decodeResetToken(String token) {
        try {
            return jwtUtil.decodePasswordResetToken(token);
        } catch (Exception e) {
            throw new AuthException("Токен сброса пароля недействителен");
        }
    }

    /* ==================== Public utility methods (used by controllers) ==================== */

    /**
     * Добавляет JWT access токен в HttpOnly cookie.
     * 
     * <p>Используется в контроллере для установки access token в cookie
     * после подтверждения email. Основной механизм работы с токенами
     * осуществляется через refresh token в cookie.</p>
     * 
     * <p><strong>Security:</strong> Cookie устанавливается с флагами:</p>
     * <ul>
     *   <li>{@code HttpOnly = true} - защита от XSS атак</li>
     *   <li>{@code Secure = true} - передача только по HTTPS</li>
     *   <li>Время жизни: 2 часа</li>
     * </ul>
     * 
     * @param token JWT access токен для сохранения в cookie
     * @param response HTTP-ответ для установки cookie
     */
    public void addJwtToCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 2); // 2 часа
        response.addCookie(cookie);
    }

    /**
     * Повторно отправляет код подтверждения на email пользователя.
     * 
     * <p>Используется когда пользователь не получил код подтверждения
     * или код истек. Генерируется новый криптографически стойкий код
     * и отправляется на email.</p>
     * 
     * <p><strong>Ограничения:</strong></p>
     * <ul>
     *   <li>Пользователь должен существовать в системе</li>
     *   <li>Аккаунт пользователя должен быть неактивирован</li>
     * </ul>
     * 
     * @param email email пользователя, на который отправляется код
     * @throws UserNotFoundException если пользователь с указанным email не найден
     * @throws UserAlreadyExistsException если аккаунт пользователя уже активирован
     * @throws MessagingException если произошла ошибка при отправке email
     * @see #register(UserSignupDto, boolean)
     * @see #confirmEmail(String, String)
     */
    public void resendConfirmationCode(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (user.isActive()) {
            throw new UserAlreadyExistsException("Пользователь уже активирован");
        }

        sendConfirmationCode(email);
    }
}
