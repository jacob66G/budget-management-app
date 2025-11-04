package com.example.budget_management_app.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    String getPublicUrl(String key);

    String upload(String pathPrefix, Long resourceId, MultipartFile file);

    void delete(String key);

    void deleteBatch(List<String> keys);

    boolean exists(String path);
}
