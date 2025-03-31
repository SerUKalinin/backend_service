package com.example.auth_service.service;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления пользователями.
 * Предоставляет методы для получения, обновления и удаления пользователей.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Получить информацию о пользователе по его имени.
     *
     * @param username имя пользователя
     * @return {@link UserDto} с данными пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    public UserDto getUserInfo(String username) {
        log.info("Запрос информации о пользователе: {}", username);
        return userRepository.findByUsername(username)
                .map(user -> new UserDto(user.getUsername(), user.getEmail()))
                .orElseThrow(() -> {
                    log.warn("Пользователь {} не найден", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    /**
     * Получить информацию о всех пользователях.
     *
     * @return список всех пользователей в виде {@link UserDto}
     */
    public List<UserDto> getAllUsersInfo() {
        log.info("Запрос информации о всех пользователях");
        return userRepository.findAll().stream()
                .map(user -> new UserDto(user.getUsername(), user.getEmail()))
                .toList(); // Java 16+
    }

    /**
     * Получить информацию о пользователе по его ID.
     *
     * @param id идентификатор пользователя
     * @return {@link UserDto} с данными пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    public UserDto getUserById(Long id) {
        log.info("Запрос информации о пользователе с ID: {}", id);
        return userRepository.findById(id)
                .map(user -> new UserDto(user.getUsername(), user.getEmail()))
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    /**
     * Обновить информацию о пользователе.
     *
     * @param username имя пользователя, которого нужно обновить
     * @param userDto  данные для обновления
     * @return обновленный {@link UserDto}
     * @throws UserNotFoundException если пользователь не найден
     */
    @Transactional
    public UserDto updateUserInfo(String username, UserDto userDto) {
        log.info("Обновление информации пользователя: {}", username);
        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setEmail(userDto.getEmail());
                    return new UserDto(user.getUsername(), user.getEmail());
                })
                .orElseThrow(() -> {
                    log.warn("Не удалось обновить. Пользователь {} не найден", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

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
