package com.example.auth_service.controller.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.user.UserUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserUpdateControllerTest {

    private UserUpdateService userUpdateService;
    private UserUpdateController userUpdateController;

    @BeforeEach
    void setUp() {
        userUpdateService = mock(UserUpdateService.class);
        userUpdateController = new UserUpdateController(userUpdateService);
    }

    @Test
    void updateFirstName_shouldReturnUpdatedUser() {
        UserDto request = new UserDto();
        request.setFirstName("NewName");
        UserDto response = new UserDto();
        response.setFirstName("NewName");

        when(userUpdateService.updateFirstName(1L, "NewName")).thenReturn(response);

        ResponseEntity<UserDto> result = userUpdateController.updateFirstName(1L, request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("NewName", result.getBody().getFirstName());
        verify(userUpdateService, times(1)).updateFirstName(1L, "NewName");
    }

    @Test
    void updateLastName_shouldReturnUpdatedUser() {
        UserDto request = new UserDto();
        request.setLastName("NewLast");
        UserDto response = new UserDto();
        response.setLastName("NewLast");

        when(userUpdateService.updateLastName(1L, "NewLast")).thenReturn(response);

        ResponseEntity<UserDto> result = userUpdateController.updateLastName(1L, request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("NewLast", result.getBody().getLastName());
        verify(userUpdateService, times(1)).updateLastName(1L, "NewLast");
    }

    @Test
    void updateEmail_shouldReturnUpdatedUser() {
        UserDto request = new UserDto();
        request.setEmail("new@example.com");
        UserDto response = new UserDto();
        response.setEmail("new@example.com");

        when(userUpdateService.updateEmail(1L, "new@example.com")).thenReturn(response);

        ResponseEntity<UserDto> result = userUpdateController.updateEmail(1L, request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("new@example.com", result.getBody().getEmail());
        verify(userUpdateService, times(1)).updateEmail(1L, "new@example.com");
    }

    @Test
    void updateRole_shouldReturnUpdatedUser() {
        UserDto request = new UserDto();
        request.setRoles(List.of("ADMIN"));
        UserDto response = new UserDto();
        response.setRoles(List.of("ADMIN"));

        when(userUpdateService.updateRole(1L, "[ADMIN]")).thenReturn(response);

        ResponseEntity<UserDto> result = userUpdateController.updateRole(1L, request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(List.of("ADMIN"), result.getBody().getRoles());
        verify(userUpdateService, times(1)).updateRole(1L, "[ADMIN]");
    }

    @Test
    void updateActiveStatus_shouldReturnUpdatedUser() {
        UserDto request = new UserDto();
        request.setActive(true);
        UserDto response = new UserDto();
        response.setActive(true);

        when(userUpdateService.updateActiveStatus(1L, true)).thenReturn(response);

        ResponseEntity<UserDto> result = userUpdateController.updateActiveStatus(1L, request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(true, result.getBody().isActive());
        verify(userUpdateService, times(1)).updateActiveStatus(1L, true);
    }
}
