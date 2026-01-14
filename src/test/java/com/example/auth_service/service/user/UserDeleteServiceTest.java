package com.example.auth_service.service.user;

import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserDeleteServiceTest {

    private UserRepository userRepository;
    private UserDeleteService userDeleteService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userDeleteService = new UserDeleteService(userRepository);
    }

    @Test
    @DisplayName("Удаление пользователя: пользователь существует")
    void deleteUser_shouldDeleteWhenUserExists() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        userDeleteService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Удаление пользователя: пользователь не найден, выбрасывается исключение")
    void deleteUser_shouldThrowExceptionWhenUserDoesNotExist() {
        Long userId = 2L;
        when(userRepository.existsById(userId)).thenReturn(false);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userDeleteService.deleteUser(userId));

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(anyLong());
    }
}
