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
     * Обновляет имя текущего пользователя.
     */
    @PutMapping("/update/first-name")
    public UserDto updateFirstName(@RequestBody String firstName, Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление имени пользователя: {}", username);
        return userService.updateFirstName(username, firstName);
    }

    /**
     * Обновляет фамилию текущего пользователя.
     */
    @PutMapping("/update/last-name")
    public UserDto updateLastName(@RequestBody String lastName, Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление фамилии пользователя: {}", username);
        return userService.updateLastName(username, lastName);
    }

    /**
     * Обновляет почту текущего пользователя.
     */
    @PutMapping("/update/email")
    public UserDto updateEmail(@RequestBody String email, Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление почты пользователя: {}", username);
        return userService.updateEmail(username, email);
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
