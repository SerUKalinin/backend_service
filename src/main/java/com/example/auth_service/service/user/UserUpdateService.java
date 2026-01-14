package com.example.auth_service.service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Сервис для обновления информации о пользователях.
 * <p>
 * Предоставляет методы для изменения email, имени, фамилии, роли и статуса активации пользователя.
 * Все операции выполняются в транзакции и используют {@link UserMapper} для возврата DTO.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserUpdateService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    /**
     * Обновляет email пользователя.
     *
     * @param userId идентификатор пользователя
     * @param email новый email
     * @return {@link UserDto} с обновлённой информацией
     * @throws IllegalArgumentException если email пустой, некорректный или уже используется другим пользователем
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    @Transactional
    public UserDto updateEmail(Long userId, String email) {
        if (email == null || !Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
            throw new IllegalArgumentException("Некорректный формат email");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email уже используется другим пользователем");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setEmail(email);
        return userMapper.toDto(user);
    }

    /**
     * Обновляет имя пользователя.
     *
     * @param userId идентификатор пользователя
     * @param firstName новое имя пользователя
     * @return {@link UserDto} с обновлённой информацией
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    @Transactional
    public UserDto updateFirstName(Long userId, String firstName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setFirstName(firstName);
        return userMapper.toDto(user);
    }

    /**
     * Обновляет фамилию пользователя.
     *
     * @param userId идентификатор пользователя
     * @param lastName новая фамилия пользователя
     * @return {@link UserDto} с обновлённой информацией
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    @Transactional
    public UserDto updateLastName(Long userId, String lastName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setLastName(lastName);
        return userMapper.toDto(user);
    }

    /**
     * Обновляет роль пользователя.
     *
     * @param userId идентификатор пользователя
     * @param roleName название роли {@link Role.RoleType}
     * @return {@link UserDto} с обновлённой информацией
     * @throws IllegalArgumentException если роль не найдена
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    @Transactional
    public UserDto updateRole(Long userId, String roleName) {
        Role.RoleType roleType = Role.RoleType.valueOf(roleName);
        Role role = roleRepository.findByRoleType(roleType)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setRoles(Set.of(role));
        return userMapper.toDto(user);
    }

    /**
     * Обновляет статус активации пользователя.
     *
     * @param userId идентификатор пользователя
     * @param active новый статус активации
     * @return {@link UserDto} с обновлённой информацией
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    @Transactional
    public UserDto updateActiveStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setActive(active);
        return userMapper.toDto(user);
    }
}
