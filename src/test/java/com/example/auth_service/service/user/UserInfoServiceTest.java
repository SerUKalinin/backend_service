//package com.example.auth_service.service.user;
//
//import com.example.auth_service.dto.UserDto;
//import com.example.auth_service.exception.UserNotFoundException;
//import com.example.auth_service.model.User;
//import com.example.auth_service.repository.UserRepository;
//import com.example.auth_service.service.user.UserInfoService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserInfoServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private UserInfoService userInfoService;
//
//    @Test
//    @DisplayName("Получение информации о пользователе по username - успешный сценарий")
//    void getUserInfo_shouldReturnUserDto_whenUserExists() {
//        // Arrange
//        String username = "testUser";
//        User user = new User();
//        user.setUsername(username);
//        user.setEmail("test@example.com");
//        user.setFirstName("Test");
//        user.setLastName("User");
//
//        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
//
//        // Act
//        UserDto result = userInfoService.getUserInfo(username);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(username, result.getUsername()); // Используем геттер getUsername()
//        assertEquals("test@example.com", result.getEmail()); // Используем геттер getEmail()
//        assertEquals("Test", result.getFirstName()); // Используем геттер getFirstName()
//        assertEquals("User", result.getLastName()); // Используем геттер getLastName()
//        verify(userRepository, times(1)).findByUsername(username);
//    }
//
//    @Test
//    @DisplayName("Получение информации о пользователе по username - пользователь не найден")
//    void getUserInfo_shouldThrowException_whenUserNotFound() {
//        // Arrange
//        String username = "nonExistingUser";
//        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(UserNotFoundException.class, () -> userInfoService.getUserInfo(username));
//        verify(userRepository, times(1)).findByUsername(username);
//    }
//
//    @Test
//    @DisplayName("Получение списка всех пользователей - успешный сценарий")
//    void getAllUserInfo_shouldReturnListOfUserDtos() {
//        // Arrange
//        User user1 = new User();
//        user1.setUsername("user1");
//        user1.setEmail("user1@example.com");
//        user1.setFirstName("User1");
//        user1.setLastName("Test");
//
//        User user2 = new User();
//        user2.setUsername("user2");
//        user2.setEmail("user2@example.com");
//        user2.setFirstName("User2");
//        user2.setLastName("Test");
//
//        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
//
//        // Act
//        List<UserDto> result = userInfoService.getAllUserInfo();
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("user1", result.get(0).getUsername());
//        assertEquals("user2", result.get(1).getUsername());
//        verify(userRepository, times(1)).findAll();
//    }
//
//    @Test
//    @DisplayName("Получение списка всех пользователей - пустой список")
//    void getAllUserInfo_shouldReturnEmptyList_whenNoUsersExist() {
//        // Arrange
//        when(userRepository.findAll()).thenReturn(List.of());
//
//        // Act
//        List<UserDto> result = userInfoService.getAllUserInfo();
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//        verify(userRepository, times(1)).findAll();
//    }
//
//    @Test
//    @DisplayName("Получение информации о пользователе по ID - успешный сценарий")
//    void getUserById_shouldReturnUserDto_whenUserExists() {
//        // Arrange
//        Long userId = 1L;
//        User user = new User();
//        user.setId(userId);
//        user.setUsername("testUser");
//        user.setEmail("test@example.com");
//        user.setFirstName("Test");
//        user.setLastName("User");
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//
//        // Act
//        UserDto result = userInfoService.getUserById(userId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("testUser", result.getUsername());
//        assertEquals("test@example.com", result.getEmail());
//        assertEquals("Test", result.getFirstName());
//        assertEquals("User", result.getLastName());
//        verify(userRepository, times(1)).findById(userId);
//    }
//
//    @Test
//    @DisplayName("Получение информации о пользователе по ID - пользователь не найден")
//    void getUserById_shouldThrowException_whenUserNotFound() {
//        // Arrange
//        Long userId = 999L;
//        when(userRepository.findById(userId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(UserNotFoundException.class, () -> userInfoService.getUserById(userId));
//        verify(userRepository, times(1)).findById(userId);
//    }
//}