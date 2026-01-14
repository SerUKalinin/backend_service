package com.example.auth_service.controller.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.user.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserInfoControllerTest {

    private UserInfoService userInfoService;
    private UserInfoController userInfoController;

    @BeforeEach
    void setUp() {
        userInfoService = mock(UserInfoService.class);
        userInfoController = new UserInfoController(userInfoService);
    }

    @Test
    @DisplayName("Получение информации о текущем пользователе по аутентификации")
    void getUserInfo_shouldReturnCurrentUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user");
        UserDto userDto = new UserDto();
        userDto.setUsername("user");

        when(userInfoService.getUserInfo("user")).thenReturn(userDto);

        ResponseEntity<UserDto> response = userInfoController.getUserInfo(authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("user", response.getBody().getUsername());
        verify(userInfoService, times(1)).getUserInfo("user");
    }

    @Test
    @DisplayName("Получение списка всех пользователей с корректным размером списка")
    void getAllUsersInfo_shouldReturnListOfUsers() {
        UserDto user1 = new UserDto(); user1.setId(1L); user1.setUsername("user1");
        UserDto user2 = new UserDto(); user2.setId(2L); user2.setUsername("user2");
        List<UserDto> users = List.of(user1, user2);

        when(userInfoService.getAllUserInfo()).thenReturn(users);

        ResponseEntity<List<UserDto>> response = userInfoController.getAllUsersInfo();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(userInfoService, times(1)).getAllUserInfo();
    }

    @Test
    @DisplayName("Получение информации о пользователе по ID с корректными данными")
    void getUserById_shouldReturnUser() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setUsername("user1");

        when(userInfoService.getUserById(1L)).thenReturn(user);

        ResponseEntity<UserDto> response = userInfoController.getUserById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getId());
        assertEquals("user1", response.getBody().getUsername());
        verify(userInfoService, times(1)).getUserById(1L);
    }
}
