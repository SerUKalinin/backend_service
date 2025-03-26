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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserDto getUserInfo(String username) {
        log.info("Запрос информации о пользователе с именем: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь с именем {} не найден", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
        return new UserDto(user.getUsername(), user.getEmail());
    }

    public List<UserDto> getAllUsersInfo() {
        log.info("Запрос информации о всех пользователях");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.warn("В базе данных пока нет пользователей");
            return List.of();
        }
        return users.stream()
                .map(user -> new UserDto(user.getUsername(), user.getEmail()))
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        log.info("Запрос информации о пользователе с ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new UserNotFoundException("Пользователь не найден");
                });
        return new UserDto(user.getUsername(), user.getEmail());
    }

    public UserDto updateUserInfo(String username, UserDto userDto) {
        log.info("Обновление информации пользователя: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Не удалось обновить. Пользователь {} не найден", username);
                    return new UserNotFoundException("Пользователь не найден");
                });

        user.setEmail(userDto.getEmail());
        userRepository.save(user);

        return new UserDto(user.getUsername(), user.getEmail());
    }

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
