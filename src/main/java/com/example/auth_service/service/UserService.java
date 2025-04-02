//package com.example.auth_service.service;
//
//import com.example.auth_service.dto.UserDto;
//import com.example.auth_service.exception.UserNotFoundException;
//import com.example.auth_service.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.regex.Pattern;
//
///**
// * Сервис для управления пользователями.
// * Предоставляет методы для получения, обновления и удаления пользователей.
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class UserService {
//
//    private final UserRepository userRepository;
//
//    /**
//     * Получить информацию о пользователе по его имени.
//     *
//     * @param username имя пользователя
//     * @return {@link UserDto} с данными пользователя
//     * @throws UserNotFoundException если пользователь не найден
//     */
//    public UserDto getUserInfo(String username) {
//        log.info("Запрос информации о пользователе: {}", username);
//        return userRepository.findByUsername(username)
//                .map(user -> new UserDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName()))
//                .orElseThrow(() -> {
//                    log.error("Пользователь {} не найден", username);
//                    return new UserNotFoundException("Пользователь не найден");
//                });
//    }
//
//    /**
//     * Получить информацию о всех пользователях.
//     *
//     * @return список всех пользователей в виде {@link UserDto}
//     */
//    public List<UserDto> getAllUsersInfo() {
//        log.info("Запрос информации о всех пользователях");
//        return userRepository.findAll().stream()
//                .map(user -> new UserDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName()))
//                .toList(); // Java 16+
//    }
//
//    /**
//     * Получить информацию о пользователе по его ID.
//     *
//     * @param id идентификатор пользователя
//     * @return {@link UserDto} с данными пользователя
//     * @throws UserNotFoundException если пользователь не найден
//     */
//    public UserDto getUserById(Long id) {
//        log.info("Запрос информации о пользователе с ID: {}", id);
//        return userRepository.findById(id)
//                .map(user -> new UserDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName()))
//                .orElseThrow(() -> {
//                    log.warn("Пользователь с ID {} не найден", id);
//                    return new UserNotFoundException("Пользователь не найден");
//                });
//    }
//
//    /**
//     * Обновить почту пользователя.
//     */
//    @Transactional
//    public UserDto updateEmail(String username, String email) {
//        log.info("Обновление почты пользователя: {}", username);
//
//        // Проверка на корректность email
//        if (email == null || !Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
//            log.error("Некорректный формат email: {}", email);
//            throw new IllegalArgumentException("Некорректный формат email");
//        }
//
//        if (userRepository.existsByEmail(email)) {
//            log.error("Email {} уже используется другим пользователем", email);
//            throw new IllegalArgumentException("Email уже используется другим пользователем");
//        }
//
//        return userRepository.findByUsername(username)
//                .map(user -> {
//                    user.setEmail(email);
//                    return new UserDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName());
//                })
//                .orElseThrow(() -> {
//                    log.error("Пользователь {} не найден для обновления почты", username);
//                    return new UserNotFoundException("Пользователь не найден");
//                });
//    }
//
//    // Обновить имя пользователя
//    @Transactional
//    public UserDto updateFirstName(String username, String firstName) {
//        log.info("Обновление имени пользователя: {}", username);
//
//        // Валидация имени
//        if (firstName == null || firstName.trim().isEmpty() || firstName.length() < 2) {
//            log.error("Имя пользователя {} не может быть короче 2 символов", username);
//            throw new IllegalArgumentException("Имя должно быть хотя бы 2 символа");
//        }
//
//        return userRepository.findByUsername(username)
//                .map(user -> {
//                    user.setFirstName(firstName);
//                    return new UserDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName());
//                })
//                .orElseThrow(() -> {
//                    log.error("Пользователь {} не найден для обновления имени", username);
//                    return new UserNotFoundException("Пользователь не найден");
//                });
//    }
//
//    // Обновить фамилию пользователя
//    @Transactional
//    public UserDto updateLastName(String username, String lastName) {
//        log.info("Обновление фамилии пользователя: {}", username);
//
//        // Валидация фамилии
//        if (lastName == null || lastName.trim().isEmpty() || lastName.length() < 2) {
//            log.error("Фамилия пользователя {} не может быть короче 2 символов", username);
//            throw new IllegalArgumentException("Фамилия должна быть хотя бы 2 символа");
//        }
//
//        return userRepository.findByUsername(username)
//                .map(user -> {
//                    user.setLastName(lastName);
//                    return new UserDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName());
//                })
//                .orElseThrow(() -> {
//                    log.error("Пользователь {} не найден для обновления фамилии", username);
//                    return new UserNotFoundException("Пользователь не найден");
//                });
//    }
//
//    /**
//     * Удалить пользователя по его ID.
//     *
//     * @param id идентификатор пользователя
//     * @throws UserNotFoundException если пользователь не найден
//     */
//    public void deleteUser(Long id) {
//        log.info("Удаление пользователя с ID: {}", id);
//        if (!userRepository.existsById(id)) {
//            log.warn("Попытка удаления несуществующего пользователя с ID: {}", id);
//            throw new UserNotFoundException("Пользователь не найден");
//        }
//        userRepository.deleteById(id);
//        log.info("Пользователь с ID {} успешно удален", id);
//    }
//}
