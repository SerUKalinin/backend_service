package com.example.auth_service.controller.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.user.UserUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для обновления данных пользователя.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/update")
public class UserUpdateController {

    private final UserUpdateService userUpdateService;

    @PutMapping("/{userId}/first-name")
    public ResponseEntity<UserDto> updateFirstName(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Обновление имени пользователя с ID: {}", userId);
        UserDto updatedUser = userUpdateService.updateFirstName(userId, userDto.getFirstName());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/last-name")
    public ResponseEntity<UserDto> updateLastName(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Обновление фамилии пользователя с ID: {}", userId);
        UserDto updatedUser = userUpdateService.updateLastName(userId, userDto.getLastName());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/email")
    public ResponseEntity<UserDto> updateEmail(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Обновление почты пользователя с ID: {}", userId);
        UserDto updatedUser = userUpdateService.updateEmail(userId, userDto.getEmail());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateRole(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Обновление роли пользователя с ID: {}", userId);
        UserDto updatedUser = userUpdateService.updateRole(userId, userDto.getRoles());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateActiveStatus(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Обновление статуса активности пользователя с ID: {}", userId);
        UserDto updatedUser = userUpdateService.updateActiveStatus(userId, userDto.isActive());
        return ResponseEntity.ok(updatedUser);
    }
}
