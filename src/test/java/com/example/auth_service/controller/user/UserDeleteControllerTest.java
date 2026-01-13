package com.example.auth_service.controller.user;

import com.example.auth_service.service.user.UserDeleteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserDeleteControllerTest {

    private UserDeleteService userDeleteService;
    private UserDeleteController userDeleteController;

    @BeforeEach
    void setUp() {
        userDeleteService = mock(UserDeleteService.class);
        userDeleteController = new UserDeleteController(userDeleteService);
    }

    @Test
    void deleteUser_shouldCallServiceAndReturnNoContent() {
        Long userId = 1L;

        ResponseEntity<Void> response = userDeleteController.deleteUser(userId);

        // Проверяем, что сервис вызван с правильным ID
        verify(userDeleteService, times(1)).deleteUser(userId);

        // Проверяем, что возвращается HTTP 204 No Content
        assertEquals(204, response.getStatusCodeValue());
    }
}
