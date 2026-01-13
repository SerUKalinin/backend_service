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

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUpdateService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

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

    @Transactional
    public UserDto updateFirstName(Long userId, String firstName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setFirstName(firstName);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateLastName(Long userId, String lastName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setLastName(lastName);
        return userMapper.toDto(user);
    }

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

    @Transactional
    public UserDto updateActiveStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setActive(active);
        return userMapper.toDto(user);
    }
}
