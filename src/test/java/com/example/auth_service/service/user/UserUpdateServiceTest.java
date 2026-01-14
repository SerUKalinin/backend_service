package com.example.auth_service.service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserUpdateServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserMapper userMapper;
    private UserUpdateService userUpdateService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        userMapper = mock(UserMapper.class);
        userUpdateService = new UserUpdateService(userRepository, roleRepository, userMapper);
    }

    @Test
    @DisplayName("Обновление email: успешно")
    void updateEmail_shouldUpdateEmailSuccessfully() {
        User user = new User();
        user.setEmail("old@example.com");
        UserDto dto = new UserDto();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userUpdateService.updateEmail(1L, "new@example.com");

        assertEquals(dto, result);
        assertEquals("new@example.com", user.getEmail());
        verify(userRepository).findById(1L);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Обновление email: некорректный email")
    void updateEmail_shouldThrowException_ifEmailInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                userUpdateService.updateEmail(1L, "invalid-email"));
    }

    @Test
    @DisplayName("Обновление email: email уже существует")
    void updateEmail_shouldThrowException_ifEmailExists() {
        when(userRepository.existsByEmail("exist@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userUpdateService.updateEmail(1L, "exist@example.com"));
    }

    @Test
    @DisplayName("Обновление имени: успешно")
    void updateFirstName_shouldUpdateSuccessfully() {
        User user = new User();
        UserDto dto = new UserDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userUpdateService.updateFirstName(1L, "John");

        assertEquals(dto, result);
        assertEquals("John", user.getFirstName());
    }

    @Test
    @DisplayName("Обновление фамилии: успешно")
    void updateLastName_shouldUpdateSuccessfully() {
        User user = new User();
        UserDto dto = new UserDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userUpdateService.updateLastName(1L, "Doe");

        assertEquals(dto, result);
        assertEquals("Doe", user.getLastName());
    }

    @Test
    @DisplayName("Обновление статуса активности: успешно")
    void updateActiveStatus_shouldUpdateSuccessfully() {
        User user = new User();
        UserDto dto = new UserDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userUpdateService.updateActiveStatus(1L, true);

        assertEquals(dto, result);
        assertTrue(user.isActive());
    }

    @Test
    @DisplayName("Обновление роли: пользователь не найден")
    void updateRole_shouldThrowUserNotFound_ifUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(roleRepository.findByRoleType(Role.RoleType.ROLE_USER))
                .thenReturn(Optional.of(new Role()));

        assertThrows(UserNotFoundException.class,
                () -> userUpdateService.updateRole(99L, "ROLE_USER"));
    }

    @Test
    @DisplayName("Обновление роли: роль не найдена")
    void updateRole_shouldThrowIllegalArgument_ifRoleDoesNotExist() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleType(Role.RoleType.ROLE_USER)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userUpdateService.updateRole(1L, "ROLE_USER"));
    }

    @Test
    @DisplayName("Обновление роли: успешно")
    void updateRole_shouldUpdateRoleSuccessfully() {
        User user = new User();
        user.setId(1L);
        Role role = new Role();
        role.setRoleType(Role.RoleType.ROLE_USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleType(Role.RoleType.ROLE_USER)).thenReturn(Optional.of(role));
        when(userMapper.toDto(any(User.class))).thenReturn(mock(UserDto.class));

        assertDoesNotThrow(() -> userUpdateService.updateRole(1L, "ROLE_USER"));
        assertEquals(Set.of(role), user.getRoles());
    }
}
