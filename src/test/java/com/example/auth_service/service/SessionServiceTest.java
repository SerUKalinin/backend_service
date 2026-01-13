import com.example.auth_service.repository.redis.RedisSessionRepository;
import com.example.auth_service.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {

    private RedisSessionRepository redisSessionRepository;
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        redisSessionRepository = mock(RedisSessionRepository.class);
        sessionService = new SessionService(redisSessionRepository);
    }

    @Test
    void saveSession_shouldCallRepository() {
        String username = "user";
        String token = "token";
        Duration duration = Duration.ofMinutes(30);

        sessionService.saveSession(username, token, duration);

        verify(redisSessionRepository).saveSession(username, token, duration);
    }

    @Test
    void isSessionValid_shouldReturnTrue_whenSessionExistsAndNotExpired() {
        String username = "user";
        String token = "token";

        when(redisSessionRepository.isSessionExists(username, token)).thenReturn(true);
        when(redisSessionRepository.isSessionExpired(username, token)).thenReturn(false);

        assertTrue(sessionService.isSessionValid(username, token));
    }

    @Test
    void isSessionValid_shouldReturnFalse_whenSessionNotExists() {
        String username = "user";
        String token = "token";

        when(redisSessionRepository.isSessionExists(username, token)).thenReturn(false);

        assertFalse(sessionService.isSessionValid(username, token));
    }

    @Test
    void isSessionValid_shouldReturnFalse_whenSessionExpired() {
        String username = "user";
        String token = "token";

        when(redisSessionRepository.isSessionExists(username, token)).thenReturn(true);
        when(redisSessionRepository.isSessionExpired(username, token)).thenReturn(true);

        assertFalse(sessionService.isSessionValid(username, token));
    }

    @Test
    void updateSession_shouldCallRepository() {
        String username = "user";
        String token = "token";
        Duration duration = Duration.ofMinutes(20);

        sessionService.updateSession(username, token, duration);

        verify(redisSessionRepository).updateSession(username, token, duration);
    }

    @Test
    void removeSession_shouldCallRepository() {
        String username = "user";

        sessionService.removeSession(username);

        verify(redisSessionRepository).removeSession(username);
    }
}
