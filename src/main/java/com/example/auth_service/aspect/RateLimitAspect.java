package com.example.auth_service.aspect;

import com.example.auth_service.annotation.RateLimit;
import com.example.auth_service.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * Аспект для обработки ограничения частоты запросов.
 * Перехватывает вызовы методов с аннотацией @RateLimit и проверяет
 * количество запросов за указанный период времени.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final RedisTemplate<String, Long> rateLimitRedisTemplate;

    /**
     * Обрабатывает вызовы методов с аннотацией @RateLimit.
     *
     * @param joinPoint Точка соединения
     * @param rateLimit Аннотация с параметрами ограничения
     * @return Результат выполнения метода
     * @throws Throwable Исключение при превышении лимита или ошибке выполнения
     */
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint, rateLimit);
        Long currentCount = rateLimitRedisTemplate.opsForValue().increment(key);
        
        if (currentCount == 1) {
            rateLimitRedisTemplate.expire(key, rateLimit.timeWindow(), TimeUnit.SECONDS);
        }
        
        if (currentCount > rateLimit.value()) {
            log.warn("Rate limit exceeded for key: {}", key);
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
        }
        
        return joinPoint.proceed();
    }

    /**
     * Генерирует ключ для Redis на основе метода и параметров запроса.
     *
     * @param joinPoint Точка соединения
     * @param rateLimit Аннотация с параметрами ограничения
     * @return Строковый ключ для Redis
     */
    private String generateKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        if (!rateLimit.key().isEmpty()) {
            return rateLimit.key();
        }

        // Получаем IP адрес из запроса
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getRemoteAddr();

        return String.format("rate_limit:%s:%s:%s", 
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName(),
            ip);
    }
} 