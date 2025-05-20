package com.example.auth_service.service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для получения информации о пользователях.
 * <p>
 * Этот сервис предоставляет методы для получения информации о пользователях как по их имени пользователя, так и по ID.
 * Также доступны методы для получения информации обо всех пользователях.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

    private final UserRepository userRepository;

    /**
     * Получить информацию о пользователе по его имени пользователя.
     * <p>
     * Метод ищет пользователя по имени и возвращает его информацию в виде {@link UserDto}.
     * Если пользователь не найден, выбрасывается исключение {@link UserNotFoundException}.
     * </p>
     *
     * @param username имя пользователя
     * @return {@link UserDto} с информацией о пользователе
     * @throws UserNotFoundException если пользователь с указанным именем не найден
     */
//    @Cacheable(value = "userInfo", key = "#username")
    public UserDto getUserInfo(String username) {
        log.info("Запрос информации пользователя: {}", username);
        return userRepository.findByUsername(username)
                .map(user -> new UserDto(
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
                ))
                .orElseThrow(() -> {
                    log.error("Пользователь {} не найден", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    /**
     * Получить информацию о всех пользователях.
     * <p>
     * Метод возвращает список {@link UserDto}, содержащий информацию о всех пользователях в системе.
     * </p>
     *
     * @return список {@link UserDto} с информацией обо всех пользователях
     */
    public List<UserDto> getAllUserInfo() {
        log.info("Запрос информации о всех пользователях");
        return userRepository.findAll().stream()
                .map(user -> new UserDto(
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
                ))
                .toList();
    }

    /**
     * Получить информацию о пользователе по его идентификатору.
     * <p>
     * Метод ищет пользователя по его ID и возвращает его информацию в виде {@link UserDto}.
     * Если пользователь не найден, выбрасывается исключение {@link UserNotFoundException}.
     * </p>
     *
     * @param id идентификатор пользователя
     * @return {@link UserDto} с информацией о пользователе
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    public UserDto getUserById(Long id) {
        log.info("Запрос информации о пользователе с ID: {}", id);
        return userRepository.findById(id)
                .map(user -> new UserDto(
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
                ))
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }
}
