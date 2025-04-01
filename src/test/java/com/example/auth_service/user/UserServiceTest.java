package com.example.auth_service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.InvalidDataException;
import com.example.auth_service.exception.UserAlreadyExistsException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // =======================
    // Тесты getUserInfo
    // =======================

    @Test
    @DisplayName("getUserInfo - Успешное получение информации о пользователе")
    void getUserInfo_Success() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserInfo(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername()); // Используем геттер
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("getUserInfo - Ошибка: пользователь не найден")
    void getUserInfo_UserNotFound() {
        String username = "nonExistingUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserInfo(username));
    }

    // =======================
    // Тесты getAllUsersInfo
    // =======================

    @Test
    @DisplayName("getAllUsersInfo - Успешное получение списка пользователей")
    void getAllUsersInfo_Success() {
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> result = userService.getAllUsersInfo();

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllUsersInfo - Пустой список пользователей")
    void getAllUsersInfo_EmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.getAllUsersInfo();

        assertTrue(result.isEmpty());
    }

    // =======================
    // Тесты getUserById
    // =======================

    @Test
    @DisplayName("getUserById - Успешное получение пользователя по ID")
    void getUserById_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, user.getId());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("getUserById - Ошибка: пользователь с ID не найден")
    void getUserById_UserNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
    }

    // =======================
    // Тесты updateFirstName
    // =======================

    @Test
    @DisplayName("updateFirstName - Успешное обновление имени")
    void updateFirstName_Success() {
        String username = "testUser";
        String newFirstName = "NewName";
        User user = new User();
        user.setUsername(username);
        user.setFirstName("OldName");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDto result = userService.updateFirstName(username, newFirstName);

        assertEquals(newFirstName, result.getFirstName()); // Используем геттер
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("updateFirstName - Ошибка: имя слишком короткое")
    void updateFirstName_TooShort() {
        String username = "testUser";
        String invalidName = "A";

        assertThrows(InvalidDataException.class,
                () -> userService.updateFirstName(username, invalidName));
    }

    // =======================
    // Тесты updateLastName
    // =======================

    @Test
    @DisplayName("updateLastName - Успешное обновление фамилии")
    void updateLastName_Success() {
        String username = "testUser";
        String newLastName = "NewLastName";
        User user = new User();
        user.setUsername(username);
        user.setLastName("OldLastName");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDto result = userService.updateLastName(username, newLastName);

        assertEquals(newLastName, result.getLastName());
        assertEquals(newLastName, user.getLastName());
    }

    @Test
    @DisplayName("updateLastName - Ошибка: фамилия содержит недопустимые символы")
    void updateLastName_InvalidCharacters() {
        // Подготовка данных
        String username = "testUser";
        String invalidLastName = "LastName123";

        // Проверяем исключение
        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> userService.updateLastName(username, invalidLastName)
        );

        // Проверяем сообщение исключения
        assertEquals("Фамилия должна содержать только буквы и дефисы", exception.getMessage());

        // Проверяем, что методы репозитория не вызывались
        verify(userRepository, never()).findByUsername(any());
        verify(userRepository, never()).save(any());
    }

    // =======================
    // Тесты updateEmail
    // =======================

    @Test
    @DisplayName("updateEmail - Успешное обновление email")
    void updateEmail_Success() {
        String username = "testUser";
        String newEmail = "new@example.com";
        User user = new User();
        user.setUsername(username);
        user.setEmail("old@example.com");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());

        UserDto result = userService.updateEmail(username, newEmail);

        assertEquals(newEmail, result.getEmail());
        assertEquals(newEmail, user.getEmail());
    }

    @Test
    @DisplayName("updateEmail - Ошибка: email уже занят другим пользователем")
    void updateEmail_AlreadyExists() {
        // Подготовка данных
        String username = "testUser";
        String currentEmail = "current@example.com";
        String existingEmail = "existing@example.com";

        // Текущий пользователь
        User currentUser = new User();
        currentUser.setUsername(username);
        currentUser.setEmail(currentEmail);

        // Другой пользователь, который уже использует этот email
        User existingUser = new User();
        existingUser.setUsername("otherUser");
        existingUser.setEmail(existingEmail);

        // Мокируем вызовы репозитория
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(existingUser));

        // Проверяем исключение
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateEmail(username, existingEmail)
        );

        // Проверяем сообщение исключения
        assertEquals("Пользователь с email 'existing@example.com' уже существует", exception.getMessage());

        // Проверяем, что email текущего пользователя не изменился
        assertEquals(currentEmail, currentUser.getEmail());
    }

    @Test
    @DisplayName("updateEmail - Успех: обновление на тот же email")
    void updateEmail_SameEmail_Success() {
        String username = "testUser";
    }

        // =======================
    // Тесты deleteUser
    // =======================

    @Test
    @DisplayName("deleteUser - Успешное удаление пользователя")
    void deleteUser_Success() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser(userId));
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("deleteUser - Ошибка: пользователь не найден")
    void deleteUser_NotFound() {
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(userId));
    }
}