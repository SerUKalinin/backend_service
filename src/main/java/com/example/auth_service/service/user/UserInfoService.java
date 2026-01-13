package com.example.auth_service.service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

    private final UserRepository userRepository;

    private UserDto mapToDto(User user) {
        List<String> roles = user.getRoles().isEmpty()
                ? List.of("ROLE_USER")
                : user.getRoles().stream()
                .map(role -> role.getRoleType().toString())
                .toList();

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roles,
                user.isActive()
        );
    }

    public UserDto getUserInfo(String username) {
        log.info("Запрос информации пользователя: {}", username);
        return userRepository.findByUsername(username)
                .map(this::mapToDto)
                .orElseThrow(() -> {
                    log.error("Пользователь {} не найден", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    public List<UserDto> getAllUserInfo() {
        log.info("Запрос информации о всех пользователях");
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public UserDto getUserById(Long id) {
        log.info("Запрос информации о пользователе с ID: {}", id);
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }
}
