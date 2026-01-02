package com.example.budget_management_app.transaction_receipts.utils;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.StorageException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class AttachmentValidator {

    private static final long MAX_FILE_SIZE_MB = 5;
    private static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    private static final Map<String, String> ALLOWED_EXTENSIONS_MAP = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png"
    );

    public String validateFileAndGetExtension(String fileName, String fileType, Long fileSize) {

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new StorageException("File name must be provided", ErrorCode.STORAGE_VALIDATION_ERROR);
        }

        if (fileType == null || fileType.trim().isEmpty()) {
            throw new StorageException("File type must be provided", ErrorCode.STORAGE_VALIDATION_ERROR);
        }

        if (fileSize == null || fileSize > MAX_FILE_SIZE_BYTES) {
            throw new StorageException("File size exceeds max file size: " + MAX_FILE_SIZE_MB + " MB", ErrorCode.STORAGE_ERROR);
        }

        String extension = extractExtension(fileName);

        if (extension == null || extension.trim().isEmpty()) {
            throw new StorageException("file extension not provided", ErrorCode.STORAGE_VALIDATION_ERROR);
        }

        if (!ALLOWED_EXTENSIONS_MAP.containsKey(extension)) {
            throw new StorageException("Invalid file extension. Allowed extensions: " + ALLOWED_EXTENSIONS_MAP.keySet(), ErrorCode.STORAGE_VALIDATION_ERROR);
        }

        String expectedFileType = ALLOWED_EXTENSIONS_MAP.get(extension);
        if (!expectedFileType.equals(fileType)) {
            throw new StorageException(
                    String.format("File type conflict. For file extension .%s, expected %s, but received %s",
                            extension, expectedFileType, fileType),
                    ErrorCode.STORAGE_VALIDATION_ERROR
            );
        }
        return extension;
    }

    private String extractExtension(String originalFilename) {
        return StringUtils.getFilenameExtension(originalFilename);
    }
}
