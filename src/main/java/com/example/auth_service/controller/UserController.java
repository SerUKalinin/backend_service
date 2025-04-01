package com.example.auth_service.controller;

import com.example.auth_service.dto.UpdateEmailRequest;
import com.example.auth_service.dto.UpdateFirstNameRequest;
import com.example.auth_service.dto.UpdateLastNameRequest;
import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/info")
    public UserDto getUserInfo(@AuthenticationPrincipal Authentication authentication) {
        String username = authentication.getName();
        log.info("Запрос информации о пользователе: {}", username);
        return userService.getUserInfo(username);
    }

    @PutMapping("/update/first-name")
    public UserDto updateFirstName(@RequestBody UpdateFirstNameRequest request, @AuthenticationPrincipal Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление имени пользователя: {}", username);
        return userService.updateFirstName(username, request.firstName());
    }

    @PutMapping("/update/last-name")
    public UserDto updateLastName(@RequestBody UpdateLastNameRequest request, @AuthenticationPrincipal Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление фамилии пользователя: {}", username);
        return userService.updateLastName(username, request.lastName());
    }

    @PutMapping("/update/email")
    public UserDto updateEmail(@RequestBody UpdateEmailRequest request, @AuthenticationPrincipal Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление почты пользователя: {}", username);
        return userService.updateEmail(username, request.email());
    }

    @GetMapping("/info/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getAllUsersInfo() {
        log.info("Запрос списка всех пользователей администратором");
        return userService.getAllUsersInfo();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Администратор запросил информацию о пользователе с ID: {}", id);
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.warn("Администратор удаляет пользователя с ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
