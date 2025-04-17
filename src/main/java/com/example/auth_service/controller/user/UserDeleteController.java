package com.example.auth_service.controller.user;

import com.example.auth_service.service.user.UserDeleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для удаления пользователей.
 * <p>
 * Этот контроллер управляет операциями удаления пользователей из системы. Доступ к удалению
 * пользователей ограничен только администраторам с ролью {@code ADMIN}.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserDeleteController {

    private final UserDeleteService userDeleteService;

    /**
     * Удаляет пользователя по его идентификатору.
     * <p>
     * Этот метод позволяет администратору удалить пользователя по его ID. Метод доступен только
     * пользователям с ролью {@code ADMIN}, что гарантируется аннотацией {@link PreAuthorize}.
     * </p>
     *
     * @param id Идентификатор пользователя, которого необходимо удалить.
     * @return Ответ без содержимого (HTTP статус 204 No Content), если удаление прошло успешно.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.warn("Администратор удаляет пользователя с ID: {}", id);
        userDeleteService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
