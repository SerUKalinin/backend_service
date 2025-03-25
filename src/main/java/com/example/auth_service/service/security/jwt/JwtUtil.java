package com.example.auth_service.service.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.auth_service.repository.redis.RedisJwtBlacklistRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${auth_service.jwtSecret}")
    private String jwtSecret;
    @Value("${auth_service.jwtLifeTimeDuration}")
    private long jwtLifeTimeDuration;
    @Value("${auth_service.issuer}")
    private String issuer;

    private final RedisJwtBlacklistRepositoryImpl redisRepository;

    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return JWT.create()
                .withSubject(username)
                .withIssuer("jwt-auth-project")
                .withClaim("roles", roles)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtLifeTimeDuration))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        if (redisRepository.isExists(token)) {
            return false;
        }

        try {
            DecodedJWT decodedJWT = decodeToken(token);
            Date expirationDate = decodeToken(token).getExpiresAt();
            return expirationDate != null && expirationDate.after(new Date());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void addJwtToBlacklist(String token) {
        Date expirationDate = decodeToken(token).getExpiresAt();
        if (expirationDate != null) {
            redisRepository.save(token, "", expirationDate);
        }
    }

    public DecodedJWT decodeToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or blank");
        }

        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(issuer)
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT;
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Invalid token" + e.getMessage());
        }
    }
}
