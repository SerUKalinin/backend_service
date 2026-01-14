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
 * REST-контроллер для управления информацией о пользователях системы.
 * <p>
 * Обеспечивает доступ к данным текущего пользователя и административный доступ к информации о всех пользователях.
 * Контроллер интегрируется с {@link UserInfoService} для получения данных и поддерживает фильтрацию по ролям с помощью Spring Security.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/info")
public class UserInfoController {

    /**
     * Сервис для работы с информацией о пользователях.
     * <p>
     * Используется для получения данных текущего пользователя, списка всех пользователей
     * и информации о пользователе по идентификатору.
     * Не допускает null, инъектируется через конструктор.
     */
    private final UserInfoService userInfoService;

    /**
     * Получает информацию о текущем пользователе системы.
     * <p>
     * Метод использует объект аутентификации для идентификации пользователя.
     * Возвращает DTO с основной информацией о пользователе.
     * </p>
     *
     * @param authentication объект аутентификации Spring Security, содержащий имя текущего пользователя;
     *                       не может быть null
     * @return {@link ResponseEntity} с {@link UserDto} информацией о текущем пользователе (HTTP 200 OK)
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
     * Доступно только пользователям с ролью {@code ADMIN}.
     * Возвращает список DTO с базовой информацией о каждом пользователе.
     * </p>
     *
     * @return {@link ResponseEntity} со списком {@link UserDto} всех пользователей (HTTP 200 OK)
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не имеет роли {@code ADMIN}
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsersInfo() {
        List<UserDto> allUsersInfo = userInfoService.getAllUserInfo();
        return ResponseEntity.ok(allUsersInfo);
    }

    /**
     * Получает детальную информацию о пользователе по его идентификатору.
     * <p>
     * Метод доступен только пользователям с ролью {@code ADMIN}.
     * Возвращает DTO с информацией о конкретном пользователе.
     * </p>
     *
     * @param id идентификатор пользователя; должен быть положительным числом
     * @return {@link ResponseEntity} с {@link UserDto} информацией о пользователе с указанным ID (HTTP 200 OK)
     * @throws com.example.auth_service.exception.UserNotFoundException если пользователь с указанным ID не найден
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не имеет роли {@code ADMIN}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto userById = userInfoService.getUserById(id);
        return ResponseEntity.ok(userById);
    }
}
