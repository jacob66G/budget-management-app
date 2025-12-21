package com.example.budget_management_app.common.storage.service;

import java.util.List;

public interface StorageService {
    String getPublicUrl(String key);

    String extractKey(String publicUrl);

    String generatePresignedPutUrl(String key, String contentType, Long fileSize);

    String generatePresignedGetUrl(String key, Long validityPeriod);

    void delete(String key);

    void deleteBatch(List<String> keys);

    boolean exists(String path);
}
