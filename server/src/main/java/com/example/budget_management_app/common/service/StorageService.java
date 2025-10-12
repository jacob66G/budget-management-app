package com.example.budget_management_app.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(Long resourceId, MultipartFile file);

    void deleteFile(String path);

    boolean exists(String path);
}
