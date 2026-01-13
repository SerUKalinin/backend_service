package com.example.auth_service.service.auth;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.exception.AuthException;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.SessionService;
import com.example.auth_service.service.redis.RedisService;
import com.example.auth_service.service.security.jwt.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthTokenServiceTest {

    private RedisService redisService;
    private SessionService sessionService;
    private JwtUtil jwtUtil;
    private UserRepository userRepository;
    private AuthTokenService tokenService;

    @BeforeEach
    void setUp() {
        redisService = mock(RedisService.class);
        sessionService = mock(SessionService.class);
        jwtUtil = mock(JwtUtil.class);
        userRepository = mock(UserRepository.class);
        tokenService = new AuthTokenService(redisService, sessionService, jwtUtil, userRepository);
    }

    @Test
    void refreshAccessToken_shouldReturnNewTokens() {
        String refreshToken = "refreshToken";
        HttpServletResponse response = mock(HttpServletResponse.class);

        User user = new User();
        user.setUsername("user1");

        Role role = new Role();
        role.setRoleType(Role.RoleType.ROLE_USER);
        user.setRoles(Set.of(role));

        when(redisService.findUsernameByRefreshToken(refreshToken)).thenReturn("user1");
        when(redisService.isRefreshTokenValid("user1", refreshToken)).thenReturn(true);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(eq(user), anyCollection())).thenReturn("newAccessToken");

        AuthResponse result = tokenService.refreshAccessToken(refreshToken, response);

        assertEquals("newAccessToken", result.getJwtToken());
        verify(redisService).deleteRefreshToken("user1", refreshToken);
        verify(redisService).saveRefreshToken(eq("user1"), anyString(), eq(Duration.ofDays(7)));
        verify(sessionService).saveSession(eq("user1"), eq("newAccessToken"), eq(Duration.ofHours(2)));
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    void refreshAccessToken_shouldThrowIfInvalidToken() {
        String refreshToken = "badToken";
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(redisService.findUsernameByRefreshToken(refreshToken)).thenReturn(null);

        assertThrows(AuthException.class, () -> tokenService.refreshAccessToken(refreshToken, response));
    }

    @Test
    void refreshToken_shouldDelegateToRefreshAccessToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("refreshToken", "token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        AuthTokenService spyService = spy(tokenService);
        AuthResponse expected = new AuthResponse("jwtToken");
        doReturn(expected).when(spyService).refreshAccessToken("token", response);

        AuthResponse actual = spyService.refreshToken(request, response);

        assertEquals(expected.getJwtToken(), actual.getJwtToken());
        verify(spyService).refreshAccessToken("token", response);
    }

    @Test
    void logout_shouldDeleteTokenAndSession() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("refreshToken", "token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(redisService.findUsernameByRefreshToken("token")).thenReturn("user1");

        tokenService.logout(request, response);

        verify(redisService).deleteRefreshToken("user1", "token");
        verify(sessionService).removeSession("user1");
        verify(response).addCookie(any(Cookie.class)); // проверка удаления cookie
    }

    @Test
    void addJwtToCookie_shouldAddCookie() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        tokenService.addJwtToCookie("jwtToken", response);
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    void validateJwtToken_shouldCheckSession() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(decodedJWT.getSubject()).thenReturn("user1");
        when(jwtUtil.decodeToken("jwtToken")).thenReturn(decodedJWT);
        when(sessionService.isSessionValid("user1", "jwtToken")).thenReturn(true);

        tokenService.validateJwtToken("jwtToken");

        verify(sessionService).updateSession("user1", "jwtToken", Duration.ofHours(2));
    }

    @Test
    void validateJwtToken_shouldThrowIfSessionInvalid() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(decodedJWT.getSubject()).thenReturn("user1");
        when(jwtUtil.decodeToken("jwtToken")).thenReturn(decodedJWT);
        when(sessionService.isSessionValid("user1", "jwtToken")).thenReturn(false);

        assertThrows(AuthException.class, () -> tokenService.validateJwtToken("jwtToken"));
    }
}
