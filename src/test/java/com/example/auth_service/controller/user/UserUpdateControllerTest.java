//package com.example.auth_service.controller.user;
//
//import com.example.auth_service.dto.UserDto;
//import com.example.auth_service.exception.UserNotFoundException;
//import com.example.auth_service.service.user.UserUpdateService;
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
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserUpdateControllerTest {
//
//    @Mock
//    private UserUpdateService userUpdateService;
//
//    @InjectMocks
//    private UserUpdateController userUpdateController;
//
//    @Test
//    @DisplayName("Обновление имени - успешный сценарий")
//    void updateFirstName_shouldReturnOkResponse_whenValidData() {
//        // Arrange
//        String username = "testUser";
//        Authentication auth = new TestingAuthenticationToken(username, "password");
//        UserDto requestDto = new UserDto();
//        requestDto.setFirstName("NewName");
//
//        UserDto responseDto = new UserDto();
//        responseDto.setUsername(username);
//        responseDto.setFirstName("NewName");
//
//        when(userUpdateService.updateFirstName(username, "NewName")).thenReturn(responseDto);
//
//        // Act
//        ResponseEntity<UserDto> response = userUpdateController.updateFirstName(requestDto, auth);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(responseDto, response.getBody());
//        verify(userUpdateService, times(1)).updateFirstName(username, "NewName");
//    }
//
//    @Test
//    @DisplayName("Обновление фамилии - успешный сценарий")
//    void updateLastName_shouldReturnOkResponse_whenValidData() {
//        // Arrange
//        String username = "testUser";
//        Authentication auth = new TestingAuthenticationToken(username, "password");
//        UserDto requestDto = new UserDto();
//        requestDto.setLastName("NewLastName");
//
//        UserDto responseDto = new UserDto();
//        responseDto.setUsername(username);
//        responseDto.setLastName("NewLastName");
//
//        when(userUpdateService.updateLastName(username, "NewLastName")).thenReturn(responseDto);
//
//        // Act
//        ResponseEntity<UserDto> response = userUpdateController.updateLastName(requestDto, auth);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(responseDto, response.getBody());
//        verify(userUpdateService, times(1)).updateLastName(username, "NewLastName");
//    }
//
//    @Test
//    @DisplayName("Обновление email - успешный сценарий")
//    void updateEmail_shouldReturnOkResponse_whenValidData() {
//        // Arrange
//        String username = "testUser";
//        Authentication auth = new TestingAuthenticationToken(username, "password");
//        UserDto requestDto = new UserDto();
//        requestDto.setEmail("new@example.com");
//
//        UserDto responseDto = new UserDto();
//        responseDto.setUsername(username);
//        responseDto.setEmail("new@example.com");
//
//        when(userUpdateService.updateEmail(username, "new@example.com")).thenReturn(responseDto);
//
//        // Act
//        ResponseEntity<UserDto> response = userUpdateController.updateEmail(requestDto, auth);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(responseDto, response.getBody());
//        verify(userUpdateService, times(1)).updateEmail(username, "new@example.com");
//    }
//
//    @Test
//    @DisplayName("Обновление данных - пользователь не найден")
//    void updateMethods_shouldThrowException_whenUserNotFound() {
//        // Arrange
//        String username = "testUser";
//        Authentication auth = new TestingAuthenticationToken(username, "password");
//        UserDto requestDto = new UserDto();
//        requestDto.setFirstName("NewName");
//
//        when(userUpdateService.updateFirstName(username, "NewName"))
//                .thenThrow(new UserNotFoundException("Пользователь не найден"));
//
//        // Act & Assert
//        assertThrows(UserNotFoundException.class,
//                () -> userUpdateController.updateFirstName(requestDto, auth));
//    }
//
//    @Test
//    @DisplayName("Обновление данных - неверные входные данные")
//    void updateMethods_shouldThrowException_whenInvalidInput() {
//        // Arrange
//        String username = "testUser";
//        Authentication auth = new TestingAuthenticationToken(username, "password");
//        UserDto invalidRequest = new UserDto();
//        invalidRequest.setFirstName("A"); // Слишком короткое имя
//
//        // Мокируем выброс исключения из сервиса
//        when(userUpdateService.updateFirstName(username, "A"))
//                .thenThrow(new IllegalArgumentException("Имя должно быть хотя бы 2 символа"));
//
//        // Act & Assert
//        assertThrows(IllegalArgumentException.class,
//                () -> userUpdateController.updateFirstName(invalidRequest, auth));
//
//        verify(userUpdateService, times(1))
//                .updateFirstName(username, "A");
//    }
//}