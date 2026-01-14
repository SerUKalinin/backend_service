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
 * Утилитный сервис для работы с JWT-токенами.
 *
 * <p>Обеспечивает генерацию, проверку, декодирование токенов, а также управление их валидностью
 * через черный список. Используется для аутентификации пользователей и управления сессиями
 * в системе.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    /**
     * Секретный ключ для подписи JWT.
     */
    @Value("${auth_service.jwtSecret}")
    private String jwtSecret;

    /**
     * Время жизни JWT в миллисекундах.
     */
    @Value("${auth_service.jwtLifeTimeDuration}")
    private long jwtLifeTimeDuration;

    /**
     * Издатель токена.
     */
    @Value("${auth_service.issuer}")
    private String issuer;

    /**
     * Репозиторий для хранения черного списка JWT.
     */
    private final RedisJwtBlacklistRepositoryImpl redisRepository;

    /**
     * Время действия токена для сброса пароля (1 час).
     */
    private static final long PASSWORD_RESET_TOKEN_EXPIRATION = 3600000;

    /**
     * Генерирует JWT-токен для пользователя на основе имени и ролей.
     *
     * @param username    Имя пользователя, для которого создается токен. Не может быть пустым или null.
     * @param authorities Коллекция полномочий пользователя. Не может быть пустой или null.
     * @return JWT-токен с указанными данными.
     * @throws IllegalArgumentException если входные параметры некорректны.
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

        return JWT.create()
                .withSubject(username)
                .withIssuer(issuer)
                .withClaim("roles", roles)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtLifeTimeDuration))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    /**
     * Генерирует JWT-токен с расширенной информацией для пользователя.
     *
     * @param user        Пользователь. Не может быть null.
     * @param authorities Коллекция полномочий пользователя. Не может быть пустой или null.
     * @return JWT-токен с расширенной информацией (ID, email, ФИО, статус активности).
     * @throws IllegalArgumentException если входные параметры некорректны.
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

        return JWT.create()
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
    }

    /**
     * Проверяет валидность токена.
     *
     * @param token JWT-токен.
     * @return true, если токен корректен, не просрочен и не находится в черном списке; false иначе.
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
     * Добавляет токен в черный список, делая его недействительным.
     *
     * @param token JWT-токен для добавления в черный список.
     * @throws IllegalArgumentException если токен пустой или недействителен.
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
     * Декодирует и проверяет JWT-токен.
     *
     * @param token JWT-токен.
     * @return Декодированный JWT.
     * @throws IllegalArgumentException если токен пустой, некорректный или просрочен.
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
     * Генерирует JWT-токен для сброса пароля.
     *
     * @param username Имя пользователя.
     * @return JWT-токен для сброса пароля с временем жизни 1 час.
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
     * @param token JWT-токен для сброса пароля.
     * @return Декодированный JWT.
     * @throws IllegalArgumentException если токен недействителен.
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
