package com.example.auth_service.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class RedisJwtBlacklistRepositoryImpl implements RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(String key, String value, Date date) {
        this.redisTemplate.opsForValue().set(key, value);
        expireAt(key, date);
    }

    @Override
    public void expireAt(String key, Date date) {
        redisTemplate.expireAt(key, date);
    }

    @Override
    public boolean isExists(String key) {
        return this.redisTemplate.hasKey(key);
    }

    @Override
    public String getValue(String key) {
        return this.redisTemplate.opsForValue().get(key);
    }
}
