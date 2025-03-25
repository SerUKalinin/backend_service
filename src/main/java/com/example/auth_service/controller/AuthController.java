package com.example.auth_service.controller;

import com.example.auth_service.dto.UserSigninDto;
import com.example.auth_service.dto.UserSignupDto;
import com.example.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-user")
    public void register(@Valid @RequestBody UserSignupDto userSignupDto) {
        authService.registerUser(userSignupDto);
    }

    @PostMapping("/register-admin")
    public void registerAdmin(@Valid @RequestBody UserSignupDto userSignupDto) {
        authService.registerAdmin(userSignupDto);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody UserSigninDto userSigninDto) {
        return authService.login(userSigninDto);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        authService.logout(extractTokenFromRequest(request));
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authHeader.substring(7);
    }
}
