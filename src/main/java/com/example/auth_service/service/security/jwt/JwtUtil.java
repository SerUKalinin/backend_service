package com.example.auth_service.service.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.redis.RedisJwtBlacklistRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Утилитный класс для работы с JWT токенами.
 * Предоставляет методы для генерации, проверки и декодирования токенов,
 * а также для добавления токенов в черный список.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    @Value("${auth_service.jwtSecret}")
    private String jwtSecret;

    @Value("${auth_service.jwtLifeTimeDuration}")
    private long jwtLifeTimeDuration;

    @Value("${auth_service.issuer}")
    private String issuer;

    private final RedisJwtBlacklistRepositoryImpl redisRepository;

    private static final long PASSWORD_RESET_TOKEN_EXPIRATION = 3600000; // 1 час

    /**
     * Генерирует JWT токен для указанного пользователя с привилегиями.
     *
     * @param username    Имя пользователя для которого генерируется токен.
     * @param authorities Привилегии пользователя.
     * @return Сгенерированный JWT токен.
     * @throws IllegalArgumentException Если {@code username} или {@code authorities} пусты или null.
     */
    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        if (username == null || username.isBlank()) {
            log.error("Имя пользователя не может быть нулевым или пустым");
            throw new IllegalArgumentException("Имя пользователя не может быть нулевым или пустым");
        }
        if (authorities == null || authorities.isEmpty()) {
            log.error("Полномочия не могут быть нулевыми или пустыми");
            throw new IllegalArgumentException("Полномочия не могут быть нулевыми или пустыми");
        }

        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
                
        log.debug("Генерация токена для пользователя {} с ролями: {}", username, roles);
        
        String token = JWT.create()
                .withSubject(username)
                .withIssuer(issuer)
                .withClaim("roles", roles)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtLifeTimeDuration))
                .sign(Algorithm.HMAC256(jwtSecret));

        return token;
    }

    /**
     * Генерирует JWT токен для указанного пользователя с расширенной информацией.
     *
     * @param user Пользователь для которого генерируется токен.
     * @param authorities Привилегии пользователя.
     * @return Сгенерированный JWT токен.
     * @throws IllegalArgumentException Если {@code user} или {@code authorities} пусты или null.
     */
    public String generateToken(User user, Collection<? extends GrantedAuthority> authorities) {
        if (user == null) {
            log.error("Пользователь не может быть нулевым");
            throw new IllegalArgumentException("Пользователь не может быть нулевым");
        }
        if (authorities == null || authorities.isEmpty()) {
            log.error("Полномочия не могут быть нулевыми или пустыми");
            throw new IllegalArgumentException("Полномочия не могут быть нулевыми или пустыми");
        }

        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
                
        log.debug("Генерация расширенного токена для пользователя {} с ролями: {}", user.getUsername(), roles);
        
        String token = JWT.create()
                .withSubject(user.getUsername())
                .withIssuer(issuer)
                .withClaim("roles", roles)
                .withClaim("userId", user.getId())
                .withClaim("email", user.getEmail())
                .withClaim("firstName", user.getFirstName())
                .withClaim("lastName", user.getLastName())
                .withClaim("active", user.isActive())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtLifeTimeDuration))
                .sign(Algorithm.HMAC256(jwtSecret));

        return token;
    }

    /**
     * Проверяет, является ли токен валидным.
     * Токен должен существовать, не быть в черном списке и не быть просроченным.
     *
     * @param token Токен для проверки.
     * @return true, если токен валиден, иначе false.
     */
    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Токен пустой или некорректный");
            return false;
        }

        if (redisRepository.isExists(token)) {
            log.warn("Токен находится в черном списке");
            return false;
        }

        try {
            DecodedJWT decodedJWT = decodeToken(token);
            Date expirationDate = decodedJWT.getExpiresAt();
            if (expirationDate == null || expirationDate.before(new Date())) {
                log.warn("Токен истек");
                return false;
            }
            return true;
        } catch (IllegalArgumentException e) {
            log.error("Некорректный токен: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Добавляет токен в черный список, чтобы он больше не использовался.
     * Токен будет недействителен до его истечения.
     *
     * @param token Токен, который нужно добавить в черный список.
     * @throws IllegalArgumentException Если токен некорректен или пуст.
     */
    public void addJwtToBlacklist(String token) {
        DecodedJWT decodedJWT = decodeToken(token);
        Date expirationDate = decodedJWT.getExpiresAt();
        if (expirationDate != null) {
            redisRepository.save(token, "blacklisted", expirationDate);
            log.debug("Токен добавлен в черный список");
        } else {
            log.warn("Не удалось добавить токен в черный список, так как его срок действия истек");
        }
    }

    /**
     * Декодирует JWT токен и возвращает его содержимое.
     * Проверяет, что токен является валидным.
     *
     * @param token Токен для декодирования.
     * @return Декодированный JWT.
     * @throws IllegalArgumentException Если токен некорректен или пуст.
     */
    public DecodedJWT decodeToken(String token) {
        if (token == null || token.isBlank()) {
            log.error("Токен не может быть нулевым или пустым");
            throw new IllegalArgumentException("Токен не может быть нулевым или пустым");
        }

        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(issuer)
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            log.debug("Токен успешно декодирован");
            return decodedJWT;
        } catch (JWTVerificationException e) {
            log.error("Недопустимый токен: {}", e.getMessage());
            throw new IllegalArgumentException("Недопустимый токен: " + e.getMessage(), e);
        }
    }

    /**
     * Генерирует токен для сброса пароля.
     *
     * @param username Имя пользователя.
     * @return Сгенерированный токен.
     */
    public String generatePasswordResetToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + PASSWORD_RESET_TOKEN_EXPIRATION))
                .sign(Algorithm.HMAC512(jwtSecret.getBytes()));
    }

    /**
     * Декодирует токен для сброса пароля.
     *
     * @param token Токен для декодирования.
     * @return Декодированный JWT.
     * @throws IllegalArgumentException Если токен недействителен.
     */
    public DecodedJWT decodePasswordResetToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC512(jwtSecret.getBytes()))
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Недействительный токен для сброса пароля");
        }
    }
}
