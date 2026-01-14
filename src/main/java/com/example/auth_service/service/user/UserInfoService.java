package com.example.auth_service.service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для получения информации о пользователях.
 * <p>
 * Предоставляет методы для получения данных о конкретном пользователе по имени пользователя или ID,
 * а также для получения информации обо всех пользователях.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Получает информацию о пользователе по его имени пользователя (username).
     *
     * @param username имя пользователя
     * @return {@link UserDto} с информацией о пользователе
     * @throws UserNotFoundException если пользователь с указанным username не найден
     */
    public UserDto getUserInfo(String username) {
        log.info("Запрос информации пользователя: {}", username);
        return userRepository.findByUsername(username)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    /**
     * Получает информацию обо всех пользователях системы.
     *
     * @return список {@link UserDto} со всеми пользователями
     */
    public List<UserDto> getAllUserInfo() {
        log.info("Запрос информации о всех пользователях");
        return userMapper.toDtoList(userRepository.findAll());
    }

    /**
     * Получает информацию о пользователе по его идентификатору (ID).
     *
     * @param id идентификатор пользователя
     * @return {@link UserDto} с информацией о пользователе
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    public UserDto getUserById(Long id) {
        log.info("Запрос информации о пользователе с ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }
}
