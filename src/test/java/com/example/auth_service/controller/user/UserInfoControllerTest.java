//package com.example.auth_service.controller.user;
//
//import com.example.auth_service.dto.UserDto;
//import com.example.auth_service.exception.UserNotFoundException;
//import com.example.auth_service.service.user.UserInfoService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.TestingAuthenticationToken;
//import org.springframework.security.core.Authentication;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserInfoControllerTest {
//
//    @Mock
//    private UserInfoService userInfoService;
//
//    @InjectMocks
//    private UserInfoController userInfoController;
//
//    @Test
//    @DisplayName("Получение информации о текущем пользователе - успешный сценарий")
//    void getUserInfo_shouldReturnUserDto() {
//        // Arrange
//        String username = "testUser";
//        Authentication authentication = new TestingAuthenticationToken(username, "password");
//        UserDto expectedDto = new UserDto(username, "test@example.com", "Test", "User");
//
//        when(userInfoService.getUserInfo(username)).thenReturn(expectedDto);
//
//        // Act
//        ResponseEntity<UserDto> response = userInfoController.getUserInfo(authentication);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(expectedDto, response.getBody());
//        verify(userInfoService, times(1)).getUserInfo(username);
//    }
//
//    @Test
//    @DisplayName("Получение информации о текущем пользователе - пользователь не найден")
//    void getUserInfo_shouldThrowException_whenUserNotFound() {
//        // Arrange
//        String username = "nonExistingUser";
//        Authentication authentication = new TestingAuthenticationToken(username, "password");
//
//        when(userInfoService.getUserInfo(username)).thenThrow(new UserNotFoundException("Пользователь не найден"));
//
//        // Act & Assert
//        assertThrows(UserNotFoundException.class, () -> userInfoController.getUserInfo(authentication));
//        verify(userInfoService, times(1)).getUserInfo(username);
//    }
//
//    @Test
//    @DisplayName("Получение списка всех пользователей (для администратора) - успешный сценарий")
//    void getAllUsersInfo_shouldReturnListOfUserDtos() {
//        // Arrange
//        UserDto user1 = new UserDto("user1", "user1@example.com", "User1", "Test");
//        UserDto user2 = new UserDto("user2", "user2@example.com", "User2", "Test");
//        List<UserDto> expectedList = List.of(user1, user2);
//
//        when(userInfoService.getAllUserInfo()).thenReturn(expectedList);
//
//        // Act
//        ResponseEntity<List<UserDto>> response = userInfoController.getAllUsersInfo();
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(expectedList, response.getBody());
//        verify(userInfoService, times(1)).getAllUserInfo();
//    }
//
//    @Test
//    @DisplayName("Получение информации о пользователе по ID (для администратора) - успешный сценарий")
//    void getUserById_shouldReturnUserDto_whenUserExists() {
//        // Arrange
//        Long userId = 1L;
//        UserDto expectedDto = new UserDto("testUser", "test@example.com", "Test", "User");
//
//        when(userInfoService.getUserById(userId)).thenReturn(expectedDto);
//
//        // Act
//        ResponseEntity<UserDto> response = userInfoController.getUserById(userId);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(expectedDto, response.getBody());
//        verify(userInfoService, times(1)).getUserById(userId);
//    }
//
//    @Test
//    @DisplayName("Получение информации о пользователе по ID (для администратора) - пользователь не найден")
//    void getUserById_shouldThrowException_whenUserNotFound() {
//        // Arrange
//        Long userId = 999L;
//
//        when(userInfoService.getUserById(userId)).thenThrow(new UserNotFoundException("Пользователь не найден"));
//
//        // Act & Assert
//        assertThrows(UserNotFoundException.class, () -> userInfoController.getUserById(userId));
//        verify(userInfoService, times(1)).getUserById(userId);
//    }
//}