package com.example.auth_service.service.user;

import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис для управления удалением пользователей из системы.
 * <p>
 * Предоставляет функционал удаления пользователя по уникальному идентификатору.
 * Сервис интегрирован с {@link UserRepository} для работы с базой данных.
 * В случае отсутствия пользователя выбрасывается бизнес-исключение {@link UserNotFoundException}.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeleteService {

    /**
     * Репозиторий для доступа к данным пользователей.
     * Используется для проверки существования пользователя и удаления из базы данных.
     */
    private final UserRepository userRepository;

    /**
     * Удаляет пользователя по уникальному идентификатору.
     * <p>
     * Метод проверяет существование пользователя в базе данных.
     * Если пользователь найден, выполняется удаление и логируется успешная операция.
     * Если пользователь отсутствует, генерируется {@link UserNotFoundException}.
     * </p>
     *
     * @param id уникальный идентификатор пользователя, которого необходимо удалить
     *           (должен быть положительным числом и существовать в базе данных)
     * @throws UserNotFoundException если пользователь с указанным идентификатором не найден
     */
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с ID: {}", id);

        if (!userRepository.existsById(id)) {
            log.warn("Попытка удаления несуществующего пользователя с ID: {}", id);
            throw new UserNotFoundException("Пользователь не найден");
        }

        userRepository.deleteById(id);
        log.info("Пользователь с ID {} успешно удален", id);
    }
}
