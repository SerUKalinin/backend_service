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
 * <p>
 * Этот контроллер предоставляет различные методы для получения информации о пользователях системы.
 * Он поддерживает запросы для получения информации о текущем пользователе, всех пользователях (для администраторов),
 * а также информации о пользователе по его ID.
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
     * Этот метод позволяет пользователю получить информацию о себе, используя данные из аутентификации.
     * Метод возвращает объект {@link UserDto}, содержащий информацию о пользователе.
     * </p>
     *
     * @param authentication Объект аутентификации, содержащий информацию о текущем пользователе.
     * @return {@link ResponseEntity} с {@link UserDto} информацией о текущем пользователе.
     */
    @GetMapping
    public ResponseEntity<UserDto> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        log.info("Запрос информации о пользователе: {}", username);
        UserDto userInfo = userInfoService.getUserInfo(username);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Получает список всех пользователей.
     * <p>
     * Этот метод доступен только администраторам и позволяет получить список всех пользователей
     * с информацией о каждом из них. Доступ ограничен ролью {@code ADMIN}.
     * </p>
     *
     * @return {@link ResponseEntity} с списком {@link UserDto} всех пользователей.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsersInfo() {
        log.info("Запрос списка всех пользователей администратором");
        List<UserDto> allUsersInfo = userInfoService.getAllUserInfo();
        return ResponseEntity.ok(allUsersInfo);
    }

    /**
     * Получает информацию о пользователе по его идентификатору.
     * <p>
     * Этот метод доступен только администраторам и позволяет получить информацию о пользователе,
     * передав его идентификатор в URL. Доступ ограничен ролью {@code ADMIN}.
     * </p>
     *
     * @param id Идентификатор пользователя.
     * @return {@link ResponseEntity} с {@link UserDto} информацией о пользователе с указанным ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Администратор запросил информацию о пользователе с ID: {}", id);
        UserDto userById = userInfoService.getUserById(id);
        return ResponseEntity.ok(userById);
    }
}
