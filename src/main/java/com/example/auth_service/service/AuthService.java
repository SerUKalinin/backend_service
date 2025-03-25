package com.example.auth_service.service;

import com.example.auth_service.dto.UserSigninDto;
import com.example.auth_service.dto.UserSignupDto;
import com.example.auth_service.exception.AuthException;
import com.example.auth_service.exception.EmailAlreadyExistsException;
import com.example.auth_service.exception.UserAlreadyExistsException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    /**
     * Регистрирует нового пользователя или администратора.
     *
     * @param userSignupDto Данные для регистрации.
     * @param isAdmin       true, если регистрируется администратор, false — пользователь.
     * @throws UserAlreadyExistsException Если пользователь с таким именем уже существует.
     * @throws EmailAlreadyExistsException Если пользователь с таким email уже существует.
     */
    public void register(UserSignupDto userSignupDto, boolean isAdmin) {
        log.info("Попытка регистрации {} с именем {}", isAdmin ? "администратора" : "пользователя", userSignupDto.getUsername());

        if (userRepository.findByUsername(userSignupDto.getUsername()).isPresent()) {
            log.warn("Пользователь с именем {} уже существует", userSignupDto.getUsername());
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        if (userRepository.findByEmail(userSignupDto.getEmail()).isPresent()) {
            log.warn("Пользователь с email {} уже существует", userSignupDto.getEmail());
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(userSignupDto.getUsername());
        user.setEmail(userSignupDto.getEmail());
        user.setPassword(passwordEncoder.encode(userSignupDto.getPassword()));

        Role.RoleType roleType = isAdmin ? Role.RoleType.ROLE_ADMIN : Role.RoleType.ROLE_USER;
        Role role = roleRepository.findByRoleType(roleType)
                .orElseThrow(() -> new IllegalStateException("Роль не найдена"));

        user.setRoles(Set.of(role));

        userRepository.save(user);

        log.info("{} с именем {} успешно зарегистрирован", isAdmin ? "Администратор" : "Пользователь", userSignupDto.getUsername());
    }


    /**
     * Выполняет вход пользователя в систему.
     *
     * @param userSigninDto Данные для входа пользователя.
     * @return JWT токен для аутентификации пользователя.
     * @throws UserNotFoundException Если пользователь с таким именем не найден.
     */
    public String login(UserSigninDto userSigninDto) {
        log.info("Попытка входа в систему для пользователя {}", userSigninDto.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userSigninDto.getUsername(), userSigninDto.getPassword())
        );

        if (authentication == null) {
            log.warn("Неверное имя пользователя или пароль для {}", userSigninDto.getUsername());
            throw new UserNotFoundException("Неверное имя пользователя или пароль");
        }

        String token = jwtUtil.generateToken(authentication.getName(), authentication.getAuthorities());
        log.info("Пользователь {} успешно авторизован", userSigninDto.getUsername());
        return token;
    }

    /**
     * Выполняет выход пользователя из системы.
     *
     * @param authHeader Заголовок Authorization, содержащий JWT токен.
     */
    public void logout(String authHeader) {
        String token = extractTokenFromAuthHeader(authHeader);
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
}
