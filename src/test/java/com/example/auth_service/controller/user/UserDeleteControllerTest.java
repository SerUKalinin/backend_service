package com.example.auth_service.controller.user;

import com.example.auth_service.service.user.UserDeleteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UserDeleteControllerTest {

    private UserDeleteService userDeleteService;
    private UserDeleteController userDeleteController;

    @BeforeEach
    void setUp() {
        userDeleteService = mock(UserDeleteService.class);
        userDeleteController = new UserDeleteController(userDeleteService);
    }

    @Test
    @DisplayName("Удаление пользователя: вызов сервиса с ID и возврат 204 No Content")
    void deleteUser_shouldCallServiceAndReturnNoContent() {
        Long userId = 1L;

        ResponseEntity<Void> response = userDeleteController.deleteUser(userId);

        verify(userDeleteService, times(1)).deleteUser(userId);

        assertEquals(204, response.getStatusCodeValue());
    }
}
