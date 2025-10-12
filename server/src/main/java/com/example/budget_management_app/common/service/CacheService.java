package com.example.budget_management_app.common.service;

public interface CacheService {

    void storeValue(RedisServiceImpl.KeyPrefix prefix, String key, Object value, Long expiration);

    Object getValue(RedisServiceImpl.KeyPrefix prefix, String key);

    void delete(RedisServiceImpl.KeyPrefix prefix, String key);
}
