package com.example.auth_service.service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
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

    public UserDto getUserInfo(String username) {
        log.info("Запрос информации пользователя: {}", username);
        return userRepository.findByUsername(username)
                .map(user -> new UserDto(
                    user.getUsername(),
                    user.getEmail(), 
                    user.getFirstName(), 
                    user.getLastName(),
                    user.getRoles(),
                    user.isActive()
                ))
                .orElseThrow(() -> {
                    log.error("Пользователь {} не найден", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    public List<UserDto> getAllUserInfo() {
        log.info("Запрос информации о всех пользователях");
        return userRepository.findAll().stream()
                .map(user -> new UserDto(
                    user.getUsername(), 
                    user.getEmail(), 
                    user.getFirstName(), 
                    user.getLastName(),
                    user.getRoles(),
                    user.isActive()
                ))
                .toList();
    }

    public UserDto getUserById(Long id) {
        log.info("Запрос информации о пользователе с ID: {}", id);
        return userRepository.findById(id)
                .map(user -> new UserDto(
                    user.getUsername(), 
                    user.getEmail(), 
                    user.getFirstName(), 
                    user.getLastName(),
                    user.getRoles(),
                    user.isActive()
                ))
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }
}
