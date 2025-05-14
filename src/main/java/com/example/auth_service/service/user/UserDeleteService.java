package com.example.auth_service.service.user;

import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис для удаления пользователя.
 * <p>
 * Этот сервис предоставляет метод для удаления пользователя по его уникальному идентификатору.
 * В случае, если пользователь не существует, выбрасывается исключение {@link UserNotFoundException}.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeleteService {

    private final UserRepository userRepository;

    /**
     * Удаляет пользователя по его уникальному идентификатору.
     * <p>
     * Метод проверяет, существует ли пользователь с данным ID в базе данных.
     * Если пользователь не найден, выбрасывается исключение {@link UserNotFoundException}.
     * В случае успешного удаления логируется соответствующее сообщение.
     * </p>
     *
     * @param id идентификатор пользователя, которого нужно удалить
     * @throws UserNotFoundException если пользователь с указанным ID не найден в базе данных
     */
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с ID: {}", id);

        // Проверка существования пользователя в базе данных
        if (!userRepository.existsById(id)) {
            log.warn("Попытка удаления несуществующего пользователя с ID: {}", id);
            throw new UserNotFoundException("Пользователь не найден");
        }

        // Удаление пользователя
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} успешно удален", id);
    }
}
