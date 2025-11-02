package com.example.budget_management_app.common.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Getter
    public enum KeyPrefix {
        REFRESH_TOKEN("refresh-token"),
        USER_SESSION("user-session"),
        VERIFICATION_CODE("verify"),
        VERIFICATION_LAST_SENT("verify:last_sent"),
        USER_DETAILS("user-details"),
        RESET_PASSWORD_CODE("reset-password"),
        RESET_PASSWORD_LAST_SENT("reset-password:last_sent");

        private final String value;

        KeyPrefix(String value) {
            this.value = value;
        }
    }

    @Override
    public void storeValue(KeyPrefix prefix, String key, Object value, Long expiration) {
        redisTemplate.opsForValue().set(buildKey(prefix, key), value, expiration, TimeUnit.SECONDS);
    }

    @Override
    public Object getValue(KeyPrefix prefix, String key) {
        return redisTemplate.opsForValue().get(buildKey(prefix, key));
    }

    @Override
    public void delete(KeyPrefix prefix, String key) {
        redisTemplate.delete(buildKey(prefix, key));
    }

    @Override
    public void delete(KeyPrefix prefix, List<String> keys) {
        List<String> keysWithPrefix = keys.stream().map(key -> buildKey(prefix, key)).toList();
        redisTemplate.delete(keysWithPrefix);
    }

    private String buildKey(KeyPrefix prefix, String key) {
        return prefix.getValue() + ":" + key;
    }
}
