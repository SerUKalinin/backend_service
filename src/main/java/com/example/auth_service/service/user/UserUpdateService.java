package com.example.auth_service.service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.Role;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUpdateService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private UserDto createUserDto(com.example.auth_service.model.User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRoles().stream()
                .findFirst()
                .map(role -> role.getRoleType().toString())
                .orElse("ROLE_USER"),
            user.isActive()
        );
    }

    @Transactional
    public UserDto updateEmail(Long userId, String email) {
        log.info("Обновление почты пользователя с ID: {}", userId);

        if (email == null || !Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
            log.error("Некорректный формат email: {}", email);
            throw new IllegalArgumentException("Некорректный формат email");
        }

        if (userRepository.existsByEmail(email)) {
            log.error("Email {} уже используется другим пользователем", email);
            throw new IllegalArgumentException("Email уже используется другим пользователем");
        }

        return userRepository.findById(userId)
                .map(user -> {
                    user.setEmail(email);
                    return createUserDto(user);
                })
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден для обновления почты", userId);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    @Transactional
    public UserDto updateFirstName(Long userId, String firstName) {
        log.info("Обновление имени пользователя с ID: {}", userId);

        // Валидация имени
        if (firstName == null || firstName.trim().isEmpty() || firstName.length() < 2) {
            log.error("Имя пользователя с ID {} не может быть короче 2 символов", userId);
            throw new IllegalArgumentException("Имя должно быть хотя бы 2 символа");
        }

        return userRepository.findById(userId)
                .map(user -> {
                    user.setFirstName(firstName);
                    return createUserDto(user);
                })
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден для обновления имени", userId);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    @Transactional
    public UserDto updateLastName(Long userId, String lastName) {
        log.info("Обновление фамилии пользователя с ID: {}", userId);

        // Валидация фамилии
        if (lastName == null || lastName.trim().isEmpty() || lastName.length() < 2) {
            log.error("Фамилия пользователя с ID {} не может быть короче 2 символов", userId);
            throw new IllegalArgumentException("Фамилия должна быть хотя бы 2 символа");
        }

        return userRepository.findById(userId)
                .map(user -> {
                    user.setLastName(lastName);
                    return createUserDto(user);
                })
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден для обновления фамилии", userId);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    @Transactional
    public UserDto updateRole(Long userId, String role) {
        log.info("Обновление роли пользователя с ID: {}", userId);

        // Валидация роли
        if (role == null || role.trim().isEmpty()) {
            log.error("Роль пользователя с ID {} не может быть пустой", userId);
            throw new IllegalArgumentException("Роль не может быть пустой");
        }

        // Проверяем, что роль существует
        Role.RoleType roleType;
        try {
            roleType = Role.RoleType.valueOf(role);
        } catch (IllegalArgumentException e) {
            log.error("Некорректная роль: {}", role);
            throw new IllegalArgumentException("Некорректная роль");
        }

        return userRepository.findById(userId)
                .map(user -> {
                    // Получаем новую роль из базы данных
                    Role newRole = roleRepository.findByRoleType(roleType)
                            .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));
                    
                    // Обновляем роли пользователя
                    user.setRoles(Set.of(newRole));
                    
                    return createUserDto(user);
                })
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден для обновления роли", userId);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    @Transactional
    public UserDto updateActiveStatus(Long userId, boolean active) {
        log.info("Обновление статуса активности пользователя с ID: {}", userId);

        return userRepository.findById(userId)
                .map(user -> {
                    user.setActive(active);
                    return createUserDto(user);
                })
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден для обновления статуса активности", userId);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }
}
