package com.example.auth_service.service.user;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserInfoServiceTest {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private UserInfoService userInfoService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userMapper = mock(UserMapper.class);
        userInfoService = new UserInfoService(userRepository, userMapper);
    }

    @Test
    void getUserInfo_shouldReturnUserDto_whenUserExists() {
        String username = "user1";
        User user = new User();
        UserDto userDto = new UserDto();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userInfoService.getUserInfo(username);

        assertEquals(userDto, result);
        verify(userRepository).findByUsername(username);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserInfo_shouldThrowException_whenUserNotFound() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userInfoService.getUserInfo(username));

        assertEquals("Пользователь не найден", ex.getMessage());
        verify(userRepository).findByUsername(username);
        verifyNoInteractions(userMapper);
    }

    @Test
    void getAllUserInfo_shouldReturnListOfUserDto() {
        User user1 = new User();
        User user2 = new User();
        List<User> users = List.of(user1, user2);
        List<UserDto> dtos = List.of(new UserDto(), new UserDto());

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDtoList(users)).thenReturn(dtos);

        List<UserDto> result = userInfoService.getAllUserInfo();

        assertEquals(dtos, result);
        verify(userRepository).findAll();
        verify(userMapper).toDtoList(users);
    }

    @Test
    void getUserById_shouldReturnUserDto_whenUserExists() {
        Long userId = 1L;
        User user = new User();
        UserDto userDto = new UserDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userInfoService.getUserById(userId);

        assertEquals(userDto, result);
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userInfoService.getUserById(userId));

        assertEquals("Пользователь не найден", ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper);
    }
}
