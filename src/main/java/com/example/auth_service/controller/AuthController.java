package com.example.auth_service.controller;

import com.example.auth_service.dto.UserSigninDto;
import com.example.auth_service.dto.UserSignupDto;
import com.example.auth_service.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


/**
 * Контроллер для аутентификации и регистрации пользователей.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Регистрация нового пользователя.
     *
     * @param userSignupDto данные пользователя для регистрации.
     */
    @PostMapping("/register-user")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody UserSignupDto userSignupDto) throws MessagingException {
        log.info("Регистрация пользователя: {}", userSignupDto.getEmail());
        authService.register(userSignupDto, false);  // false - для обычного пользователя
    }

    /**
     * Регистрация нового администратора.
     *
     * @param userSignupDto данные администратора для регистрации.
     */
    @PostMapping("/register-admin")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAdmin(@Valid @RequestBody UserSignupDto userSignupDto) throws MessagingException {
        log.info("Регистрация администратора: {}", userSignupDto.getEmail());
        authService.register(userSignupDto, true);  // true - для администратора
    }

    /**
     * Аутентификация пользователя.
     *
     * @param userSigninDto данные для входа.
     * @return JWT-токен.
     */
    @PostMapping("/login")
    public String login(@Valid @RequestBody UserSigninDto userSigninDto) {
        log.info("Аутентификация пользователя: {}", userSigninDto.getUsername());
        return authService.login(userSigninDto);
    }

    /**
     * Выход пользователя из системы.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Выход пользователя, токен: {}", authHeader);
        authService.logout(authHeader);
    }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public void verifyEmail(@RequestParam String email, @RequestParam String code) {
        log.info("Получен запрос на проверку email: {}, code: {}", email, code);
        authService.confirmEmail(email, code);
    }
}
