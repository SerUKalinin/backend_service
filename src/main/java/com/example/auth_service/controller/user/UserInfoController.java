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
 * REST-контроллер для получения информации о пользователях системы.
 * <p>
 * Предоставляет методы для:
 * <ul>
 *     <li>Получения информации о текущем пользователе</li>
 *     <li>Получения списка всех пользователей (только для администратора)</li>
 *     <li>Получения информации о пользователе по ID (только для администратора)</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/info")
public class UserInfoController {

    private final UserInfoService userInfoService;

    /**
     * Получает информацию о текущем пользователе.
     * <p>
     * Метод использует объект аутентификации для определения текущего пользователя.
     * Возвращает {@link UserDto} с информацией о пользователе.
     * </p>
     *
     * @param authentication Объект аутентификации с данными текущего пользователя
     * @return {@link ResponseEntity} с {@link UserDto} информации о текущем пользователе (HTTP 200 OK)
     */
    @GetMapping
    public ResponseEntity<UserDto> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        UserDto userInfo = userInfoService.getUserInfo(username);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Получает список всех пользователей системы.
     * <p>
     * Метод доступен только администраторам (роль {@code ADMIN}) и возвращает список
     * всех пользователей с базовой информацией.
     * </p>
     *
     * @return {@link ResponseEntity} со списком {@link UserDto} всех пользователей (HTTP 200 OK)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsersInfo() {
        List<UserDto> allUsersInfo = userInfoService.getAllUserInfo();
        return ResponseEntity.ok(allUsersInfo);
    }

    /**
     * Получает информацию о пользователе по его идентификатору.
     * <p>
     * Метод доступен только администраторам (роль {@code ADMIN}). Позволяет получить
     * детальную информацию о пользователе по его ID.
     * </p>
     *
     * @param id Идентификатор пользователя
     * @return {@link ResponseEntity} с {@link UserDto} информацией о пользователе с указанным ID (HTTP 200 OK)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto userById = userInfoService.getUserById(id);
        return ResponseEntity.ok(userById);
    }
}
