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
 * Аспект для ограничения частоты вызовов методов приложения.
 *
 * Применяется к методам, помеченным аннотацией {@link RateLimit}.
 * Служит для защиты публичных API и внутренних сервисов от избыточных запросов,
 * предотвращая перегрузку системы. Учёт и контроль выполняются с использованием Redis.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    /**
     * RedisTemplate для хранения счётчиков вызовов.
     *
     * Используется для инкрементации и отслеживания количества запросов
     * по ключу, формируемому на основе метода, класса и IP-адреса клиента
     * либо заданному явно в аннотации {@link RateLimit}.
     */
    private final RedisTemplate<String, Long> rateLimitRedisTemplate;

    /**
     * Перехватывает вызов метода и проверяет соблюдение лимита запросов.
     *
     * Инкрементирует счётчик запросов в Redis. Если текущее количество
     * превышает значение {@link RateLimit#value()}, выбрасывает
     * {@link RateLimitExceededException}.
     *
     * @param joinPoint точка соединения AOP, содержащая информацию о целевом методе
     * @param rateLimit аннотация с настройками ограничения частоты вызовов
     * @return результат выполнения целевого метода при соблюдении лимита
     * @throws RateLimitExceededException если превышен допустимый лимит вызовов
     * @throws Throwable если целевой метод выбрасывает исключение
     */
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint, rateLimit);
        Long currentCount = rateLimitRedisTemplate.opsForValue().increment(key);

        if (currentCount == 1) {
            rateLimitRedisTemplate.expire(key, rateLimit.timeWindow(), TimeUnit.SECONDS);
        }

        if (currentCount > rateLimit.value()) {
            log.warn("Превышен лимит запросов для ключа: {}", key);
            throw new RateLimitExceededException("Превышен лимит запросов. Попробуйте позже.");
        }

        return joinPoint.proceed();
    }

    /**
     * Формирует уникальный ключ для хранения счётчика вызовов в Redis.
     *
     * Если аннотация {@link RateLimit} содержит явный ключ — используется он.
     * В противном случае ключ создаётся на основе имени класса, метода
     * и IP-адреса клиента.
     *
     * @param joinPoint точка соединения AOP, используемая для получения информации о методе
     * @param rateLimit аннотация с настройками ограничения частоты вызовов
     * @return строка ключа Redis для учёта запросов
     */
    private String generateKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        if (!rateLimit.key().isEmpty()) {
            return rateLimit.key();
        }

        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getRemoteAddr();

        return String.format(
                "rate_limit:%s:%s:%s",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ip
        );
    }
}
