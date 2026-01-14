package com.example.auth_service.controller.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.user.UserUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для обновления информации о пользователях.
 *
 * Обеспечивает возможности изменения персональных данных пользователя
 * и управляемых администратором свойств, включая роль и статус активности.
 * Контроллер используется в пользовательском и административном интерфейсах
 * для управления учетными записями.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/update")
public class UserUpdateController {

    /**
     * Сервис для обновления данных пользователей.
     * Отвечает за бизнес-логику изменения свойств пользователей в системе.
     */
    private final UserUpdateService userUpdateService;

    /**
     * Обновляет имя пользователя.
     *
     * @param userId  уникальный идентификатор пользователя
     * @param userDto объект DTO, содержащий новое имя пользователя
     * @return {@link ResponseEntity} с обновлённым {@link UserDto}, HTTP 200 OK
     */
    @PutMapping("/{userId}/first-name")
    public ResponseEntity<UserDto> updateFirstName(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Обновление имени пользователя с ID: {}", userId);
        UserDto updatedUser = userUpdateService.updateFirstName(userId, userDto.getFirstName());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Обновляет фамилию пользователя.
     *
     * @param userId  уникальный идентификатор пользователя
     * @param userDto объект DTO, содержащий новую фамилию пользователя
     * @return {@link ResponseEntity} с обновлённым {@link UserDto}, HTTP 200 OK
     */
    @PutMapping("/{userId}/last-name")
    public ResponseEntity<UserDto> updateLastName(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Обновление фамилии пользователя с ID: {}", userId);
        UserDto updatedUser = userUpdateService.updateLastName(userId, userDto.getLastName());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Обновляет email пользователя.
     *
     * @param userId  уникальный идентификатор пользователя
     * @param userDto объект DTO, содержащий новый email пользователя
     * @return {@link ResponseEntity} с обновлённым {@link UserDto}, HTTP 200 OK
     */
    @PutMapping("/{userId}/email")
    public ResponseEntity<UserDto> updateEmail(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Обновление почты пользователя с ID: {}", userId);
        UserDto updatedUser = userUpdateService.updateEmail(userId, userDto.getEmail());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Обновляет роль пользователя.
     *
     * Доступно только администраторам с ролью {@code ADMIN}.
     *
     * @param userId  уникальный идентификатор пользователя
     * @param userDto объект DTO, содержащий новую роль пользователя
     * @return {@link ResponseEntity} с обновлённым {@link UserDto}, HTTP 200 OK
     */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateRole(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Обновление роли пользователя с ID: {}", userId);
        UserDto updatedUser = userUpdateService.updateRole(userId, userDto.getRoles().toString());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Обновляет статус активности пользователя.
     *
     * Доступно только администраторам с ролью {@code ADMIN}.
     *
     * @param userId  уникальный идентификатор пользователя
     * @param userDto объект DTO, содержащий новый статус активности
     * @return {@link ResponseEntity} с обновлённым {@link UserDto}, HTTP 200 OK
     */
    @PutMapping("/{userId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateActiveStatus(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Обновление статуса активности пользователя с ID: {}", userId);
        UserDto updatedUser = userUpdateService.updateActiveStatus(userId, userDto.isActive());
        return ResponseEntity.ok(updatedUser);
    }
}
