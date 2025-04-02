package com.example.auth_service.controller.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.user.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для получения информации о пользователях.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/info")
public class UserInfoController {

    private final UserInfoService userInfoService;

    /**
     * Получает информацию о текущем пользователе.
     */
    @GetMapping
    public UserDto getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        log.info("Запрос информации о пользователе: {}", username);
        return userInfoService.getUserInfo(username);
    }

    /**
     * Получает информацию обо всех пользователях (только для администратора).
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getAllUsersInfo() {
        log.info("Запрос списка всех пользователей администратором");
        return userInfoService.getAllUserInfo();
    }

    /**
     * Получает информацию о конкретном пользователе по ID (только для администратора).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Администратор запросил информацию о пользователе с ID: {}", id);
        return userInfoService.getUserById(id);
    }
}