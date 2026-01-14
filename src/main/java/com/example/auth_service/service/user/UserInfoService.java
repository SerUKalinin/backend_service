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
 * Предоставляет методы для получения данных о конкретном пользователе по username или ID,
 * а также для получения информации обо всех пользователях. Использует {@link UserRepository}
 * для доступа к данным и {@link UserMapper} для преобразования сущностей в DTO.
 * Бросает {@link UserNotFoundException}, если пользователь не найден.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Маппер для преобразования сущностей {@link User} в {@link UserDto}.
     */
    private final UserMapper userMapper;

    /**
     * Получает информацию о пользователе по его username.
     *
     * @param username Логин пользователя. Не может быть null или пустым.
     * @return {@link UserDto} с информацией о пользователе.
     * @throws UserNotFoundException если пользователь с указанным username не найден.
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
     * @return Список {@link UserDto} с информацией о всех пользователях.
     */
    public List<UserDto> getAllUserInfo() {
        log.info("Запрос информации о всех пользователях");
        return userMapper.toDtoList(userRepository.findAll());
    }

    /**
     * Получает информацию о пользователе по его уникальному идентификатору.
     *
     * @param id Уникальный идентификатор пользователя. Не может быть null.
     * @return {@link UserDto} с информацией о пользователе.
     * @throws UserNotFoundException если пользователь с указанным ID не найден.
     */
    public UserDto getUserById(Long id) {
        log.info("Запрос информации о пользователе с ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }
}
