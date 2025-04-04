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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

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
    public AuthResponse login(UserSigninDto userSigninDto) {
        log.info("Попытка входа в систему для пользователя {}", userSigninDto.getUsername());

        // Проверка, если введенный username является email
        String usernameOrEmail = userSigninDto.getUsername();
        User user;

        if (usernameOrEmail.contains("@")) {  // Если это email
            user = userRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь с таким email не найден"));
        } else {  // Если это логин
            user = userRepository.findByUsername(usernameOrEmail)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь с таким логином не найден"));
        }

        // Аутентификация пользователя через Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), userSigninDto.getPassword())
        );

        // Проверка активности пользователя
        if (!user.isActive()) {
            log.warn("Пользователь {} не активирован", userSigninDto.getUsername());
            throw new UserNotActivatedException("Пользователь не активирован. Пожалуйста, подтвердите ваш email.");
        }

        // Генерация JWT токена
        String token = jwtUtil.generateToken(authentication.getName(), authentication.getAuthorities());

        // Сохраняем сессию в Redis
        sessionService.saveSession(user.getUsername(), token, Duration.ofHours(2));

        log.info("Пользователь {} успешно авторизован", userSigninDto.getUsername());

        // Возвращаем объект AuthResponse, а не просто строку
        return new AuthResponse(token);
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
    public void confirmEmail(String email, String code) {
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
    }

    /**
     * Выполняет выход пользователя из системы.
     *
     * @param authHeader Заголовок Authorization, содержащий JWT токен.
     */
    public void logout(String authHeader) {
        String token = extractTokenFromAuthHeader(authHeader);
        if (token == null) {
            log.warn("JWT токен отсутствует или имеет неверный формат");
            throw new IllegalArgumentException("Неверный формат токена");
        }

        log.info("Попытка выхода пользователя с токеном {}", token);
        jwtUtil.addJwtToBlacklist(token);
        log.info("Пользователь с токеном {} успешно вышел из системы", token);
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
        String resetLink = "http://localhost:63342/auth_service/auth_frontend/reset-password.html?token=" + token;

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
    public void resetPassword(String token, String newPassword) {
        log.info("Запрос на сброс пароля с токеном");

        try {
            // Декодируем токен и получаем имя пользователя
            DecodedJWT decodedJWT = jwtUtil.decodePasswordResetToken(token);
            String username = decodedJWT.getSubject();

            // Находим пользователя
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

            // Проверяем токен в Redis
            if (!redisService.checkPasswordResetToken(user.getEmail(), token)) {
                log.warn("Недействительный или истекший токен для сброса пароля");
                throw new AuthException("Недействительный или истекший токен для сброса пароля");
            }

            // Обновляем пароль
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Удаляем использованный токен из Redis
            redisService.deletePasswordResetToken(user.getEmail());

            log.info("Пароль успешно сброшен для пользователя: {}", username);
        } catch (Exception e) {
            log.error("Ошибка при сбросе пароля: {}", e.getMessage());
            throw new AuthException("Ошибка при сбросе пароля");
        }
    }

}
