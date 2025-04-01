package com.example.auth_service.service;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.InvalidDataException;
import com.example.auth_service.exception.UserAlreadyExistsException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления пользователями.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private UserDto toDto(User user) {
        return new UserDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

    public UserDto getUserInfo(String username) {
        log.info("Запрос информации о пользователе: {}", username);
        return userRepository.findByUsername(username)
                .map(this::toDto)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с именем '%s' не найден".formatted(username)));
    }

    public List<UserDto> getAllUsersInfo() {
        log.info("Запрос информации о всех пользователях");
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    public UserDto getUserById(Long id) {
        log.info("Запрос информации о пользователе с ID: {}", id);
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID %d не найден".formatted(id)));
    }

    @Transactional
    public UserDto updateFirstName(String username, String firstName) {
        validateName(firstName, "Имя");
        log.info("Обновление имени пользователя: {}", username);

        User user = getUserByUsername(username);
        user.setFirstName(firstName);

        return toDto(user);
    }

    @Transactional
    public UserDto updateLastName(String username, String lastName) {
        validateName(lastName, "Фамилия");
        log.info("Обновление фамилии пользователя: {}", username);

        User user = getUserByUsername(username);
        user.setLastName(lastName);

        return toDto(user);
    }

    @Transactional
    public UserDto updateEmail(String username, String email) {
        validateEmail(email);
        log.info("Обновление почты пользователя: {}", username);

        User user = getUserByUsername(username);

        // Проверяем, не пытаемся ли установить тот же email
        if (email.equals(user.getEmail())) {
            return toDto(user);
        }

        // Проверяем, не занят ли email другим пользователем
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Пользователь с email '%s' уже существует".formatted(email));
        }

        user.setEmail(email);
        return toDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующего пользователя с ID: {}", id);
            throw new UserNotFoundException("Пользователь с ID %d не найден".formatted(id));
        }
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} удален", id);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с именем '%s' не найден".formatted(username)));
    }

    private void validateName(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new InvalidDataException(fieldName + " не может быть пустым");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new InvalidDataException(fieldName + " должна содержать от 2 до 50 символов");
        }
        if (!name.matches("[a-zA-Zа-яА-Я\\-\\s]+")) {
            throw new InvalidDataException(fieldName + " должна содержать только буквы и дефисы");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidDataException("Email не может быть пустым");
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new InvalidDataException("Некорректный формат email: " + email);
        }
    }
}
