package com.example.budget_management_app.common.storage.service;

import com.example.budget_management_app.transaction.dto.TransactionReceiptUploadRequest;
import com.example.budget_management_app.transaction.dto.TransactionReceiptUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    String getPublicUrl(String key);

    String extractKey(String publicUrl);

    TransactionReceiptUploadResponse generateUploadUrl(TransactionReceiptUploadRequest uploadRequest);

    String upload(String pathPrefix, Long resourceId, MultipartFile file);

    void delete(String key);

    void deleteBatch(List<String> keys);

    boolean exists(String path);
}
