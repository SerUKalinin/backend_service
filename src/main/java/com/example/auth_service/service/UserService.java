package com.example.auth_service.service;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с пользователями.
 * Обрабатывает получение информации о пользователях.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Получает информацию о пользователе по его имени пользователя.
     *
     * @param username Имя пользователя.
     * @return Информация о пользователе.
     * @throws UserNotFoundException Если пользователь с таким именем не найден.
     */
    public UserDto getUserInfo(String username) {
        log.info("Запрос информации о пользователе с именем: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь с именем {} не найден", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
        return new UserDto(user.getUsername(), user.getEmail());
    }

    /**
     * Получает информацию о всех пользователях.
     *
     * @return Список DTO с информацией о всех пользователях.
     */
    public List<UserDto> getAllUsersInfo() {
        log.info("Запрос информации о всех пользователях");

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.warn("Не найдено пользователей в системе");
            throw new UserNotFoundException("Не найдено пользователей");
        }

        return users.stream()
                .map(user -> new UserDto(user.getUsername(), user.getEmail()))
                .collect(Collectors.toList());
    }
}
