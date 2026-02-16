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
 * Аспект ограничения частоты вызовов методов (rate limiting).
 *
 * <p>Перехватывает вызовы методов, помеченных аннотацией {@link RateLimit},
 * и контролирует количество обращений в заданном временном окне.</p>
 *
 * <p>Основное назначение аспекта — защита публичных API и чувствительных операций
 * от избыточной нагрузки, brute-force атак и неконтролируемых повторных вызовов.</p>
 *
 * <p>Учёт количества вызовов осуществляется с использованием Redis.
 * Для каждого уникального ключа хранится счётчик с ограниченным временем жизни (TTL).</p>
 *
 * <p>Ключ ограничения формируется на основе:
 * <ul>
 *     <li>явно заданного логического ключа в {@link RateLimit#key()} (если указан),</li>
 *     <li>или имени класса и метода,</li>
 *     <li>и IP-адреса клиента.</li>
 * </ul>
 * Таким образом, лимит применяется индивидуально для каждого клиента.</p>
 *
 * <p>Аспект корректно обрабатывает ситуации отсутствия HTTP-контекста
 * (например, при вызове из асинхронных задач или тестов),
 * используя fallback-значение IP.</p>
 */

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    /**
     * RedisTemplate для хранения и управления счётчиками запросов.
     *
     * <p>Используется для атомарного инкремента счётчика вызовов и контроля
     * времени жизни ключей (TTL), соответствующих временным окнам ограничений.</p>
     *
     * <p>Каждый ключ Redis представляет собой отдельный rate limit,
     * связанный с конкретным методом и клиентом.</p>
     */
    private final RedisTemplate<String, Long> rateLimitRedisTemplate;

    /**
     * Перехватывает выполнение целевого метода и проверяет соблюдение лимита вызовов.
     *
     * <p>При каждом вызове:</p>
     * <ol>
     *     <li>Формируется ключ ограничения.</li>
     *     <li>Счётчик вызовов атомарно инкрементируется в Redis.</li>
     *     <li>Проверяется и при необходимости устанавливается TTL ключа.</li>
     *     <li>При превышении допустимого лимита выбрасывается исключение.</li>
     * </ol>
     *
     * <p>Если текущее количество вызовов превышает значение,
     * заданное в {@link RateLimit#limit()},
     * выполнение метода блокируется.</p>
     *
     * @param joinPoint точка соединения AOP, содержащая информацию о вызываемом методе
     * @param rateLimit аннотация с параметрами ограничения частоты вызовов
     * @return результат выполнения целевого метода при соблюдении лимита
     * @throws RateLimitExceededException если превышен допустимый лимит вызовов
     * @throws Throwable если целевой метод выбрасывает исключение
     */

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint, rateLimit);

        Long currentCount = rateLimitRedisTemplate.opsForValue().increment(key);

        Long ttl = rateLimitRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl == -1) {
            rateLimitRedisTemplate.expire(key, rateLimit.timeWindow(), TimeUnit.SECONDS);
        }

        if (currentCount > rateLimit.limit()) {
            log.warn(
                    "Rate limit exceeded. key={}, count={}, limit={}, window={}s",
                    key, currentCount, rateLimit.limit(), rateLimit.timeWindow()
            );
            throw new RateLimitExceededException("Превышен лимит запросов. Попробуйте позже.");
        }

        return joinPoint.proceed();
    }


    /**
     * Формирует уникальный ключ для хранения счётчика вызовов в Redis.
     *
     * <p>Алгоритм формирования ключа:</p>
     * <ul>
     *     <li>Если в {@link RateLimit#key()} задано значение — используется
     *     логический ключ в сочетании с IP-адресом клиента.</li>
     *     <li>Если ключ не задан — используется имя класса, метода и IP-адрес клиента.</li>
     * </ul>
     *
     * <p>В случае отсутствия HTTP-контекста используется fallback-значение IP,
     * что позволяет безопасно применять аспект вне web-слоя.</p>
     *
     * @param joinPoint точка соединения AOP, содержащая информацию о целевом методе
     * @param rateLimit аннотация с настройками ограничения частоты вызовов
     * @return строковый ключ Redis для учёта количества вызовов
     */

    private String generateKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String ip = (attributes != null)
                ? attributes.getRequest().getRemoteAddr()
                : "unknown";

        if (!rateLimit.key().isEmpty()) {
            return String.format("rate_limit:%s:%s", rateLimit.key(), ip);
        }

        return String.format(
                "rate_limit:%s:%s:%s",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ip
        );
    }

}
