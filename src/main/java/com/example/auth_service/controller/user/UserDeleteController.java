package com.example.auth_service.controller.user;

import com.example.auth_service.service.user.UserDeleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для управления удалением пользователей из системы.
 * <p>
 * Все операции удаления доступны только администраторам с ролью {@code ADMIN}.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserDeleteController {

    private final UserDeleteService userDeleteService;

    /**
     * Удаляет пользователя по идентификатору.
     * <p>
     * Метод доступен только администраторам (роль {@code ADMIN}) благодаря аннотации
     * {@link PreAuthorize}. При успешном удалении возвращает HTTP статус {@code 204 No Content}.
     * </p>
     *
     * @param id Идентификатор пользователя, которого необходимо удалить
     * @return {@link ResponseEntity} без содержимого с HTTP статусом 204
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.warn("Администратор удаляет пользователя с ID: {}", id);
        userDeleteService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
