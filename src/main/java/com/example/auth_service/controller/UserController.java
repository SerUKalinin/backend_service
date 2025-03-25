package com.example.auth_service.controller;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для работы с пользователями.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Получает информацию о текущем пользователе.
     *
     * @param authentication объект аутентификации, содержащий информацию о текущем пользователе.
     * @return DTO с данными текущего пользователя.
     */
    @GetMapping("/info")
    public UserDto getUserInfo(Authentication authentication) {
        return userService.getUserInfo(authentication.getName());
    }

    /**
     * Получает информацию о всех пользователях.
     * Доступ разрешен только администраторам.
     *
     * @return список DTO с данными всех пользователей.
     */
    @GetMapping("/info/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getAllUsersInfo() {
        return userService.getAllUsersInfo();
    }
}
