package com.example.auth_service.controller.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.user.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<UserDto> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        log.info("Запрос информации о пользователе: {}", username);
        UserDto userInfo = userInfoService.getUserInfo(username);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsersInfo() {
        log.info("Запрос списка всех пользователей администратором");
        List<UserDto> allUsersInfo = userInfoService.getAllUserInfo();
        return ResponseEntity.ok(allUsersInfo);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Администратор запросил информацию о пользователе с ID: {}", id);
        UserDto userById = userInfoService.getUserById(id);
        return ResponseEntity.ok(userById);
    }
}
