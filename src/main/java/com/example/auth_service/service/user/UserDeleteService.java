package com.example.auth_service.service.user;

import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeleteService {

    private final UserRepository userRepository;

    /**
     * Удалить пользователя по его ID.
     *
     * @param id идентификатор пользователя
     * @throws UserNotFoundException если пользователь не найден
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
