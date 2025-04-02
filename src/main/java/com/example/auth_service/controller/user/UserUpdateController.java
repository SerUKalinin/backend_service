package com.example.auth_service.controller.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.user.UserUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @PutMapping("/first-name")
    public ResponseEntity<UserDto> updateFirstName(@RequestBody UserDto userDto, Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление имени пользователя: {}", username);
        UserDto updatedUser = userUpdateService.updateFirstName(username, userDto.getFirstName());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/last-name")
    public ResponseEntity<UserDto> updateLastName(@RequestBody UserDto userDto, Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление фамилии пользователя: {}", username);
        UserDto updatedUser = userUpdateService.updateLastName(username, userDto.getLastName());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/email")
    public ResponseEntity<UserDto> updateEmail(@RequestBody UserDto userDto, Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление почты пользователя: {}", username);
        UserDto updatedUser = userUpdateService.updateEmail(username, userDto.getEmail());
        return ResponseEntity.ok(updatedUser);
    }
}
