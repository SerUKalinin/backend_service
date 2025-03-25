package com.example.auth_service.controller;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public UserDto getUserInfo(Authentication authentication) {
        return userService.getUserInfoByUsername(authentication.getName());
    }

    @GetMapping("/info/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getAllUsersInfo() {
        return userService.getAllUsersInfo();
    }
}
