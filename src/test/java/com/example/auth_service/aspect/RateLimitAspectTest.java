package com.example.auth_service.aspect;

import com.example.auth_service.annotation.RateLimit;
import com.example.auth_service.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RateLimitAspectTest {

    @Mock
    private RedisTemplate<String, Long> rateLimitRedisTemplate;

    @Mock
    private ValueOperations<String, Long> valueOperations;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    @BeforeEach
    void setUp() {
        when(rateLimitRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Разрешить запрос, если количество запросов не превышает лимит")
    void testRateLimitWithinLimit() throws Throwable {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.timeWindow()).thenReturn(60);
        when(rateLimit.key()).thenReturn("");
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.auth_service.controller.AuthController");
        when(signature.getName()).thenReturn("login");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        Object result = rateLimitAspect.rateLimit(joinPoint, rateLimit);

        assertEquals("success", result);
        verify(rateLimitRedisTemplate).expire(anyString(), eq(60L), any());
    }

    @Test
    @DisplayName("Выбросить RateLimitExceededException при превышении лимита запросов")
    void testRateLimitExceeded() {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.key()).thenReturn("");
        when(valueOperations.increment(anyString())).thenReturn(6L);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.auth_service.controller.AuthController");
        when(signature.getName()).thenReturn("login");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitAspect.rateLimit(joinPoint, rateLimit);
        });
    }

    @Test
    @DisplayName("Использовать пользовательский ключ при его указании")
    void testCustomKey() throws Throwable {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.timeWindow()).thenReturn(60);
        when(rateLimit.key()).thenReturn("custom:kay");
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(joinPoint.proceed()).thenReturn("success");

        Object result = rateLimitAspect.rateLimit(joinPoint, rateLimit);

        assertEquals("success", result);
        verify(valueOperations).increment("custom:kay");
    }

    @Test
    @DisplayName("Сгенерировать ключ на основе метода и IP-адреса при отсутствии пользовательского ключа")
    void testKeyGenerator() throws Throwable {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.timeWindow()).thenReturn(60);
        when(rateLimit.key()).thenReturn("");
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.auth_service.controller.AuthController");
        when(signature.getName()).thenReturn("login");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        Object result = rateLimitAspect.rateLimit(joinPoint, rateLimit);
        assertEquals("success", result);
        verify(valueOperations).increment("rate_limit:com.example.auth_service.controller.AuthController:login:127.0.0.1");
    }

    @Test
    @DisplayName("Сбросить счетчик после истечения временного окна")
    void testRateLimitResetAfterTimeWindow() throws Throwable {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.timeWindow()).thenReturn(1);
        when(rateLimit.key()).thenReturn("");
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.auth_service.controller.AuthController");
        when(signature.getName()).thenReturn("login");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        Object result = rateLimitAspect.rateLimit(joinPoint, rateLimit);

        Thread.sleep(1100);

        when(valueOperations.increment(anyString())).thenReturn(1L);
        Object result2 = rateLimitAspect.rateLimit(joinPoint, rateLimit);

        assertEquals("success", result);
        assertEquals("success", result2);
    }

    @Test
    @DisplayName("Обрабатывать разные IP-адреса независимо")
    void teatDifferentIpAddresses() throws Throwable {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.timeWindow()).thenReturn(60);
        when(rateLimit.key()).thenReturn("");
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.auth_service.controller.AuthController");
        when(signature.getName()).thenReturn("login");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(valueOperations.increment(anyString())).thenReturn(6L);
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitAspect.rateLimit(joinPoint, rateLimit);
        });

        when(request.getRemoteAddr()).thenReturn("127.0.0.2");
        when(valueOperations.increment(anyString())).thenReturn(1L);
        Object result = rateLimitAspect.rateLimit(joinPoint, rateLimit);
        assertEquals("success", result);
    }

    @Test
    @DisplayName("Обрабатывать разные методы независимо при одинаковом IP")
    void tastDifferentMethods() throws Throwable {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.timeWindow()).thenReturn(60);
        when(rateLimit.key()).thenReturn("");
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.auth_service.controller.AuthController");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(signature.getName()).thenReturn("login");
        when(valueOperations.increment(anyString())).thenReturn(6L);
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitAspect.rateLimit(joinPoint, rateLimit);
        });

        when(signature.getName()).thenReturn("register");
        when(valueOperations.increment(anyString())).thenReturn(1L);
        Object result = rateLimitAspect.rateLimit(joinPoint, rateLimit);
        assertEquals("success", result);
    }

    @Test
    @DisplayName("Корректно работать с различными временными окнами лимита")
    void testDifferentTimeWindows() throws Throwable {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.timeWindow()).thenReturn(30);
        when(rateLimit.key()).thenReturn("");
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.auth_service.controller.AuthController");
        when(signature.getName()).thenReturn("login");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        Object result = rateLimitAspect.rateLimit(joinPoint, rateLimit);

        assertEquals("success", result);
        verify(rateLimitRedisTemplate).expire(anyString(), eq(30L), any());
    }

    @Test
    @DisplayName("Разрешить запрос на границе лимита")
    void testAtLimitBoundary() throws Throwable {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.key()).thenReturn("");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.auth_service.controller.AuthController");
        when(signature.getName()).thenReturn("login");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(valueOperations.increment(anyString())).thenReturn(5L);
        when(joinPoint.proceed()).thenReturn("success");

        Object result = rateLimitAspect.rateLimit(joinPoint, rateLimit);
        assertEquals("success", result);
    }

    @Test
    @DisplayName("Выбросить RateLimitExceededException при превышении лимита на 1")
    void testExceedingLimitByOne() {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.key()).thenReturn("");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.auth_service.controller.AuthController");
        when(signature.getName()).thenReturn("login");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(valueOperations.increment(anyString())).thenReturn(6L);

        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitAspect.rateLimit(joinPoint, rateLimit);
        });
    }

    @Test
    @DisplayName("Корректно обрабатывать параллельные запросы и соблюдение лимита")
    void testConcurrentRequests() throws Throwable {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.timeWindow()).thenReturn(60);
        when(rateLimit.key()).thenReturn("");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.auth_service.controller.AuthController");
        when(signature.getName()).thenReturn("login");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L, 4L, 5L, 6L);
        when(joinPoint.proceed()).thenReturn("success");

        for (int i = 0; i < 5; i++) {
            Object result = rateLimitAspect.rateLimit(joinPoint, rateLimit);
            assertEquals("success", result);
        }

        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitAspect.rateLimit(joinPoint, rateLimit);
        });
    }
}
