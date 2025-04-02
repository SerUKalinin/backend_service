package com.example.auth_service.controller.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.user.UserUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Обновляет имя текущего пользователя.
     */
    @PutMapping("/first-name")
    public UserDto updateFirstName(@RequestBody UserDto userDto, Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление имени пользователя: {}", username);
        return userUpdateService.updateFirstName(username, userDto.getFirstName());
    }

    /**
     * Обновляет фамилию текущего пользователя.
     */
    @PutMapping("/last-name")
    public UserDto updateLastName(@RequestBody UserDto userDto, Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление фамилии пользователя: {}", username);
        return userUpdateService.updateLastName(username, userDto.getLastName());
    }

    /**
     * Обновляет почту текущего пользователя.
     */
    @PutMapping("/email")
    public UserDto updateEmail(@RequestBody UserDto userDto, Authentication authentication) {
        String username = authentication.getName();
        log.info("Обновление почты пользователя: {}", username);
        return userUpdateService.updateEmail(username, userDto.getEmail());
    }
}
