package com.example.auth_service.service.user;

import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.user.UserDeleteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDeleteServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDeleteService userDeleteService;

    @Test
    @DisplayName("Удаление существующего пользователя - успешное выполнение")
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        userDeleteService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя - выбрасывает исключение")
    void deleteUser_WhenUserNotExists_ShouldThrowException() {
        // Arrange
        Long userId = 99L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userDeleteService.deleteUser(userId);
        });

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }
}