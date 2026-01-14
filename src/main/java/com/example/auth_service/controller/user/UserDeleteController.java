package com.example.auth_service.controller.user;

import com.example.auth_service.service.user.UserDeleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для удаления пользователей системы.
 * <p>
 * Контроллер предоставляет эндпоинт для удаления пользователя по идентификатору.
 * Все операции защищены ролями Spring Security и доступны только администраторам
 * с ролью {@code ADMIN}. Логирует операции удаления для аудита.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserDeleteController {

    /**
     * Сервис для удаления пользователей.
     * <p>
     * Отвечает за бизнес-логику удаления пользователя из системы.
     * Не допускает null, инъектируется через конструктор.
     */
    private final UserDeleteService userDeleteService;

    /**
     * Удаляет пользователя по указанному идентификатору.
     * <p>
     * Метод доступен только администраторам (роль {@code ADMIN}).
     * При успешном удалении возвращает HTTP статус 204 (No Content).
     * Операция логируется с уровнем WARN для обеспечения аудита действий администратора.
     * </p>
     *
     * @param id идентификатор пользователя для удаления; должен быть положительным числом
     * @return {@link ResponseEntity} без содержимого с HTTP статусом 204 при успешном удалении
     * @throws com.example.auth_service.exception.UserNotFoundException если пользователь с указанным ID не найден
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не имеет роли {@code ADMIN}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.warn("Администратор удаляет пользователя с ID: {}", id);
        userDeleteService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
