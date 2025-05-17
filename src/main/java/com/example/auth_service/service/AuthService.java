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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для аутентификации и регистрации пользователей.
 * Обрабатывает регистрацию пользователей и администраторов, а также вход и выход из системы.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

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
     * @param userSignupDto Данные для регистрации.
     * @param isAdmin       true, если регистрируется администратор, false — пользователь.
     * @throws UserAlreadyExistsException Если пользователь с таким именем уже существует.
     * @throws EmailAlreadyExistsException Если пользователь с таким email уже существует.
     * @throws MessagingException Если произошла ошибка при отправке письма.
     */
    public void register(UserSignupDto userSignupDto, boolean isAdmin) throws MessagingException {
        log.info("Попытка регистрации {} с именем {}", isAdmin ? "администратора" : "пользователя", userSignupDto.getUsername());

        // Проверка существования пользователя с таким же именем
        if (userRepository.findByUsername(userSignupDto.getUsername()).isPresent()) {
            log.warn("Пользователь с именем {} уже существует", userSignupDto.getUsername());
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        // Проверка существования пользователя с таким же email
        if (userRepository.findByEmail(userSignupDto.getEmail()).isPresent()) {
            log.warn("Пользователь с email {} уже существует", userSignupDto.getEmail());
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }

        // Создание нового пользователя
        User user = new User();
        user.setUsername(userSignupDto.getUsername());
        user.setEmail(userSignupDto.getEmail());
        user.setPassword(passwordEncoder.encode(userSignupDto.getPassword()));
        user.setActive(false); // Устанавливаем флаг активности в false
        user.setFirstName(userSignupDto.getFirstName()); // Устанавливаем имя
        user.setLastName(userSignupDto.getLastName()); // Устанавливаем фамилию

        // Назначение роли для пользователя
        Role.RoleType roleType = isAdmin ? Role.RoleType.ROLE_ADMIN : Role.RoleType.ROLE_USER;
        Role role = roleRepository.findByRoleType(roleType)
                .orElseThrow(() -> new IllegalStateException("Роль не найдена"));

        user.setRoles(Set.of(role));

        // Сохранение пользователя в базу данных
        userRepository.save(user);

        // Генерация и отправка кода подтверждения
        String code = generateConfirmationCode();
        redisService.saveConfirmationCode(userSignupDto.getEmail(), code);
        emailService.sendConfirmationCode(userSignupDto.getEmail(), code);

        log.info("{} с именем {} успешно зарегистрирован. Код подтверждения отправлен на почту.",
                isAdmin ? "Администратор" : "Пользователь", userSignupDto.getUsername());
    }

    /**
     * Генерация случайного кода подтверждения.
     *
     * @return сгенерированный код подтверждения.
     */
    private String generateConfirmationCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    /**
     * Выполняет вход пользователя в систему.
     *
     * @param userSigninDto Данные для входа пользователя.
     * @return JWT токен для аутентификации пользователя.
     * @throws UserNotFoundException Если пользователь с таким именем не найден.
     * @throws UserNotActivatedException Если пользователь не активирован.
     */
    public AuthResponse login(UserSigninDto userSigninDto, HttpServletResponse response) {
        log.info("Попытка входа в систему для пользователя {}", userSigninDto.getUsername());
        String usernameOrEmail = userSigninDto.getUsername();
        User user;
        if (usernameOrEmail.contains("@")) {
            user = userRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь с таким email не найден"));
        } else {
            user = userRepository.findByUsername(usernameOrEmail)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь с таким логином не найден"));
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), userSigninDto.getPassword())
        );
        if (!user.isActive()) {
            log.warn("Пользователь {} не активирован", userSigninDto.getUsername());
            throw new UserNotActivatedException("Пользователь не активирован. Пожалуйста, подтвердите ваш email.");
        }
        String accessToken = jwtUtil.generateToken(
                user,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getRoleType().name()))
                        .collect(Collectors.toList())
        );
        // Генерация refresh token
        String refreshToken = UUID.randomUUID().toString();
        // Сохраняем refresh token в Redis
        saveRefreshToken(user.getUsername(), refreshToken, Duration.ofDays(7));
        // Устанавливаем JWT и refresh token в cookie
        addJwtToCookie(accessToken, response);
        addRefreshTokenToCookie(refreshToken, response);
        // Сохраняем access token в сессии (если нужно)
        sessionService.saveSession(user.getUsername(), accessToken, Duration.ofHours(2));
        log.info("Пользователь {} успешно авторизован", userSigninDto.getUsername());
        return new AuthResponse(accessToken, refreshToken);
    }

    public void addJwtToCookie(String token, HttpServletResponse response) {
        // Добавляем JWT в cookie
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setHttpOnly(true);  // Невозможность доступа к cookie через JS
        cookie.setPath("/");  // Чтобы cookie было доступно на всех маршрутах
        cookie.setMaxAge(60 * 60 * 2);  // Пример: токен живет 2 часа
        response.addCookie(cookie);
    }

    /**
     * Подтверждение email пользователя.
     *
     * @param email Email пользователя.
     * @param code  Код подтверждения.
     * @throws InvalidConfirmationCodeException Если код неверный или истекший.
     * @throws UserNotFoundException Если пользователь не найден.
     */
    public AuthResponse confirmEmail(String email, String code) {
        log.info("Проверка кода подтверждения для email: {}", email);

        // Проверка кода подтверждения в Redis
        if (!redisService.checkConfirmationCode(email, code)) {
            log.warn("Неверный или истекший код подтверждения для email: {}", email);
            throw new InvalidConfirmationCodeException("Неверный или истекший код подтверждения");
        }

        log.info("Код подтверждения для email: {} верный", email);

        // Найти пользователя по email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        // Активировать пользователя
        user.setActive(true);
        userRepository.save(user);

        // Удалить код подтверждения из Redis
        redisService.deleteConfirmationCode(email);

        log.info("Email пользователя {} успешно подтвержден. Аккаунт активирован.", email);

        // Генерация JWT токена
        String token = jwtUtil.generateToken(
                user,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getRoleType().name()))
                        .collect(Collectors.toList())
        );

        // Сохраняем сессию в Redis
        sessionService.saveSession(user.getUsername(), token, Duration.ofHours(2)); // Сессия на 2 часа

        log.info("Пользователь {} успешно активирован и авторизован", email);

        // Генерация refresh token
        String refreshToken = UUID.randomUUID().toString();
        saveRefreshToken(user.getUsername(), refreshToken, Duration.ofDays(7));

        // Возвращаем объект AuthResponse с токенами
        return new AuthResponse(token, refreshToken);
    }

    /**
     * Выполняет выход пользователя из системы.
     *
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken != null) {
            String username = redisService.findUsernameByRefreshToken(refreshToken);
            if (username != null) {
                deleteRefreshToken(username, refreshToken);
            }
        }
        // Удалить cookie у клиента
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Извлекает JWT-токен из заголовка Authorization.
     *
     * @param authHeader Заголовок Authorization.
     * @return JWT-токен.
     * @throws AuthException если заголовок Authorization отсутствует или имеет неверный формат.
     */
    private String extractTokenFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthException("Некорректный формат заголовка Authorization");
        }
        return authHeader.substring(7);
    }

    public void resendConfirmationCode(String email) throws MessagingException {
        log.info("Запрос на повторную отправку кода подтверждения для email: {}", email);

        // Проверка, существует ли пользователь с таким email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким email не найден"));

        // Проверка, активирован ли уже пользователь
        if (user.isActive()) {
            log.warn("Пользователь с email {} уже активирован", email);
            throw new UserAlreadyExistsException("Пользователь с таким email уже активирован");
        }

        // Генерация нового кода подтверждения
        String code = generateConfirmationCode();

        // Сохранение нового кода в Redis
        redisService.saveConfirmationCode(email, code);

        // Отправка нового кода подтверждения на email
        emailService.sendConfirmationCode(email, code);

        log.info("Новый код подтверждения был успешно отправлен на email: {}", email);
    }

    /**
     * Отправляет инструкции для сброса пароля на email пользователя.
     *
     * @param email Email пользователя.
     * @throws MessagingException Если произошла ошибка при отправке письма.
     * @throws UserNotFoundException Если пользователь с таким email не найден.
     */
    public void sendPasswordResetLink(String email) throws MessagingException {
        log.info("Запрос на сброс пароля для email: {}", email);

        // Проверка, существует ли пользователь с таким email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким email не найден"));

        // Генерация токена для сброса пароля
        String token = jwtUtil.generatePasswordResetToken(user.getUsername());

        // Сохранение токена в Redis с временем жизни 1 час
        redisService.savePasswordResetToken(email, token, Duration.ofHours(1));

        // Формирование ссылки для сброса пароля
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        // Отправка письма со ссылкой
        emailService.sendPasswordResetEmail(email, resetLink);

        log.info("Ссылка для сброса пароля отправлена на email: {}", email);
    }

    /**
     * Сбрасывает пароль пользователя.
     *
     * @param token Токен для сброса пароля.
     * @param newPassword Новый пароль.
     * @throws AuthException Если токен недействителен или истек срок его действия.
     */
    public AuthResponse resetPassword(String token, String newPassword) {
        log.info("Запрос на сброс пароля с токеном");

        try {
            // Декодируем токен и получаем имя пользователя
            DecodedJWT decodedJWT = jwtUtil.decodePasswordResetToken(token);
            String username = decodedJWT.getSubject();

            // Находим пользователя
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

            // Сбрасываем пароль
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Удаляем токен сброса пароля из Redis
            redisService.deletePasswordResetToken(user.getEmail());

            log.info("Пароль для пользователя {} успешно сброшен", username);

            // Генерация нового токена и возвращение в AuthResponse
            String newToken = jwtUtil.generateToken(
                    user,
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getRoleType().name()))
                            .collect(Collectors.toList())
            );

            // Генерация refresh token
            String refreshToken = UUID.randomUUID().toString();
            
            // Сохраняем refresh token в Redis
            saveRefreshToken(user.getUsername(), refreshToken, Duration.ofDays(7));

            // Создаем новую сессию для пользователя
            sessionService.saveSession(username, newToken, Duration.ofHours(2));

            return new AuthResponse(newToken, refreshToken);

        } catch (Exception e) {
            log.error("Ошибка при сбросе пароля: {}", e.getMessage());
            throw new AuthException("Ошибка при сбросе пароля");
        }
    }

    /**
     * Добавляет refresh token в HttpOnly cookie с настройками безопасности.
     *
     * @param refreshToken refresh токен, который нужно сохранить в cookie
     * @param response     HTTP-ответ, в который добавляется cookie
     */
    public void addRefreshTokenToCookie(String refreshToken, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Только по HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) Duration.ofDays(7).getSeconds());

        response.addCookie(refreshTokenCookie);

        log.debug("Добавлен refresh token в cookie: имя = {}, срок жизни = {} секунд",
                refreshTokenCookie.getName(), refreshTokenCookie.getMaxAge());
    }

    /**
     * Сохраняет refresh токен в Redis с указанным временем жизни.
     *
     * @param username     имя пользователя
     * @param refreshToken refresh токен
     * @param duration     время жизни токена
     */
    public void saveRefreshToken(String username, String refreshToken, Duration duration) {
        redisService.saveRefreshToken(username, refreshToken, duration);
        log.debug("Сохранён refresh token для пользователя: {} на срок {} секунд", username, duration.getSeconds());
    }

    /**
     * Проверяет наличие и валидность refresh токена в Redis.
     *
     * @param username     имя пользователя
     * @param refreshToken refresh токен
     * @return true, если токен валиден, иначе false
     */
    public boolean isRefreshTokenValid(String username, String refreshToken) {
        boolean valid = redisService.isRefreshTokenValid(username, refreshToken);
        log.debug("Проверка валидности refresh token для пользователя {}: {}", username, valid);
        return valid;
    }

    /**
     * Удаляет refresh токен из Redis.
     *
     * @param username     имя пользователя
     * @param refreshToken refresh токен
     */
    public void deleteRefreshToken(String username, String refreshToken) {
        redisService.deleteRefreshToken(username, refreshToken);
        log.debug("Удалён refresh token для пользователя: {}", username);
    }

    /**
     * Обновляет access токен на основе переданного refresh токена из cookie.
     *
     * @param refreshToken refresh токен из cookie
     * @param response     HTTP-ответ для установки новой cookie
     * @return новый access токен в обёртке AuthResponse или null, если refresh токен недействителен
     */
    public AuthResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        String username = redisService.findUsernameByRefreshToken(refreshToken);
        if (username == null || !isRefreshTokenValid(username, refreshToken)) {
            log.warn("Попытка обновления токена: refresh token невалиден или не найден");
            return null;
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Пользователь с именем {} не найден при обновлении токена", username);
                    return new UserNotFoundException("Пользователь не найден");
                });

        String accessToken = jwtUtil.generateToken(
                user,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getRoleType().name()))
                        .collect(Collectors.toList())
        );
        log.debug("Сгенерирован новый access token для пользователя: {}", username);

        // Ротация refresh токена
        String newRefreshToken = UUID.randomUUID().toString();
        saveRefreshToken(user.getUsername(), newRefreshToken, Duration.ofDays(7));
        addRefreshTokenToCookie(newRefreshToken, response);
        deleteRefreshToken(user.getUsername(), refreshToken);
        sessionService.saveSession(user.getUsername(), accessToken, Duration.ofHours(2));

        log.info("Успешное обновление access и refresh токенов для пользователя: {}", username);
        return new AuthResponse(accessToken, newRefreshToken);
    }
}
