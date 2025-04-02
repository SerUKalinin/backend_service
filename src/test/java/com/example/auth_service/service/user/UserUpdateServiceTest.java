package com.example.auth_service.service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.user.UserUpdateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUpdateServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserUpdateService userUpdateService;

    @Test
    @DisplayName("Обновление email - успешный сценарий")
    void updateEmail_shouldReturnUpdatedUserDto_whenValidData() {
        // Arrange
        String username = "testUser";
        String newEmail = "new@example.com";
        User user = new User();
        user.setUsername(username);
        user.setEmail("old@example.com");
        user.setFirstName("Test");
        user.setLastName("User");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);

        // Act
        UserDto result = userUpdateService.updateEmail(username, newEmail);

        // Assert
        assertNotNull(result);
        assertEquals(newEmail, result.getEmail());
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).existsByEmail(newEmail);
    }

    @Test
    @DisplayName("Обновление email - некорректный формат")
    void updateEmail_shouldThrowException_whenInvalidEmailFormat() {
        // Arrange
        String username = "testUser";
        String invalidEmail = "invalid-email";

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userUpdateService.updateEmail(username, invalidEmail));
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("Обновление email - email уже используется")
    void updateEmail_shouldThrowException_whenEmailAlreadyInUse() {
        // Arrange
        String username = "testUser";
        String existingEmail = "existing@example.com";

        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userUpdateService.updateEmail(username, existingEmail));
        verify(userRepository, times(1)).existsByEmail(existingEmail);
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("Обновление имени - успешный сценарий")
    void updateFirstName_shouldReturnUpdatedUserDto_whenValidData() {
        // Arrange
        String username = "testUser";
        String newFirstName = "NewName";
        User user = new User();
        user.setUsername(username);
        user.setEmail("test@example.com");
        user.setFirstName("OldName");
        user.setLastName("User");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDto result = userUpdateService.updateFirstName(username, newFirstName);

        // Assert
        assertNotNull(result);
        assertEquals(newFirstName, result.getFirstName());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Обновление имени - слишком короткое имя")
    void updateFirstName_shouldThrowException_whenNameTooShort() {
        // Arrange
        String username = "testUser";
        String shortName = "A";

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userUpdateService.updateFirstName(username, shortName));
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("Обновление фамилии - успешный сценарий")
    void updateLastName_shouldReturnUpdatedUserDto_whenValidData() {
        // Arrange
        String username = "testUser";
        String newLastName = "NewLastName";
        User user = new User();
        user.setUsername(username);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("OldLastName");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDto result = userUpdateService.updateLastName(username, newLastName);

        // Assert
        assertNotNull(result);
        assertEquals(newLastName, result.getLastName());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Обновление фамилии - слишком короткая фамилия")
    void updateLastName_shouldThrowException_whenLastNameTooShort() {
        // Arrange
        String username = "testUser";
        String shortLastName = "B";

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userUpdateService.updateLastName(username, shortLastName));
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("Обновление данных - пользователь не найден")
    void updateMethods_shouldThrowUserNotFoundException_whenUserNotFound() {
        // Arrange
        String username = "nonExistingUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert for all update methods
        assertThrows(UserNotFoundException.class,
                () -> userUpdateService.updateEmail(username, "test@example.com"));
        assertThrows(UserNotFoundException.class,
                () -> userUpdateService.updateFirstName(username, "Name"));
        assertThrows(UserNotFoundException.class,
                () -> userUpdateService.updateLastName(username, "LastName"));

        verify(userRepository, times(3)).findByUsername(username);
    }
}