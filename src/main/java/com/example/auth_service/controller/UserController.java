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
     */
    @GetMapping("/info")
    public UserDto getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        log.info("Запрос информации о пользователе: {}", username);
        return userService.getUserInfo(username);
    }

    /**
     * Обновляет информацию о текущем пользователе.
     */
    @PutMapping("/update")
    public UserDto updateUserInfo(@RequestBody UserDto userDto, Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление информации о пользователе: {}", username);
        return userService.updateUserInfo(username, userDto);
    }

    /**
     * Получает информацию обо всех пользователях (только для администратора).
     */
    @GetMapping("/info/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getAllUsersInfo() {
        log.info("Запрос списка всех пользователей администратором");
        return userService.getAllUsersInfo();
    }

    /**
     * Получает информацию о конкретном пользователе по ID (только для администратора).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Администратор запросил информацию о пользователе с ID: {}", id);
        return userService.getUserById(id);
    }

    /**
     * Удаляет пользователя (только для администратора).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        log.warn("Администратор удаляет пользователя с ID: {}", id);
        userService.deleteUser(id);
    }
}
