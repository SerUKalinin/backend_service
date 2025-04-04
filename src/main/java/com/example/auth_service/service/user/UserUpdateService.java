package com.example.auth_service.service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUpdateService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto updateEmail(String username, String email) {
        log.info("Обновление почты пользователя: {}", username);

        if (email == null || !Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
            log.error("Некорректный формат email: {}", email);
            throw new IllegalArgumentException("Некорректный формат email");
        }

        if (userRepository.existsByEmail(email)) {
            log.error("Email {} уже используется другим пользователем", email);
            throw new IllegalArgumentException("Email уже используется другим пользователем");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setEmail(email);
                    return new UserDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRoles());
                })
                .orElseThrow(() -> {
                    log.error("Пользователь {} не найден для обновления почты", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    @Transactional
    public UserDto updateFirstName(String username, String firstName) {
        log.info("Обновление имени пользователя: {}", username);

        // Валидация имени
        if (firstName == null || firstName.trim().isEmpty() || firstName.length() < 2) {
            log.error("Имя пользователя {} не может быть короче 2 символов", username);
            throw new IllegalArgumentException("Имя должно быть хотя бы 2 символа");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setFirstName(firstName);
                    return new UserDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRoles());
                })
                .orElseThrow(() -> {
                    log.error("Пользователь {} не найден для обновления имени", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    @Transactional
    public UserDto updateLastName(String username, String lastName) {
        log.info("Обновление фамилии пользователя: {}", username);

        // Валидация фамилии
        if (lastName == null || lastName.trim().isEmpty() || lastName.length() < 2) {
            log.error("Фамилия пользователя {} не может быть короче 2 символов", username);
            throw new IllegalArgumentException("Фамилия должна быть хотя бы 2 символа");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setLastName(lastName);
                    return new UserDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRoles());
                })
                .orElseThrow(() -> {
                    log.error("Пользователь {} не найден для обновления фамилии", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }
}
