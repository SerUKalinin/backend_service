package com.example.auth_service.repository.redis;

import java.util.Date;

public interface RedisRepository {
    void save(String key, String value, Date date);
    void expireAt(String key, Date date);
    boolean isExists(String key);
    String getValue(String key);
}
