package com.example.budget_management_app.common.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    @Getter
    public enum KeyPrefix {
        REFRESH_TOKEN("refreshToken"),
        USER_SESSION("userSession");

        private final String value;

        KeyPrefix(String value) {
            this.value = value;
        }
    }

    public void storeValue(KeyPrefix prefix, String key, String value, Long expiration) {
        redisTemplate.opsForValue().set(buildKey(prefix, key), value, expiration, TimeUnit.SECONDS);
    }

    public String getValue(KeyPrefix prefix, String key) {
        return redisTemplate.opsForValue().get(buildKey(prefix, key));
    }

    public void delete(KeyPrefix prefix, String key) {
        redisTemplate.delete(buildKey(prefix, key));
    }

    private String buildKey(KeyPrefix prefix, String key) {
        return prefix.getValue() + ":" + key;
    }
}
