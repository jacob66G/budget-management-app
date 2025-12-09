package com.example.budget_management_app.common.storage.service;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.StorageException;
import com.example.budget_management_app.transaction.dto.TransactionReceiptUploadRequest;
import com.example.budget_management_app.transaction.dto.TransactionReceiptUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Presigner S3Presigner;
    private final S3Client s3Client;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    @Value("${aws.region}")
    private String region;

    private static final long MAX_FILE_SIZE_MB = 5;
    private static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png"
    );

    private static final Map<String, String> ALLOWED_EXTENSIONS_MAP = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png"
    );

    @Override
    public String upload(String pathPrefix, Long resourceId, MultipartFile file) {
        validateFile(file);

        try {
            String key = generateKey(pathPrefix, resourceId, file.getOriginalFilename());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();


            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            return key;

        } catch (IOException e) {
            throw new StorageException("Failed to read file bytes", ErrorCode.STORAGE_ERROR, e);
        } catch (S3Exception e) {
            throw new StorageException("S3 upload failed", ErrorCode.STORAGE_ERROR, e);
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            throw new StorageException("S3 delete failed for key: " + key, ErrorCode.STORAGE_ERROR, e);
        }
    }

    @Override
    public void deleteBatch(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        try {
            List<ObjectIdentifier> objectsToDelete = keys.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .toList();

            Delete deletePayload = Delete.builder()
                    .objects(objectsToDelete)
                    .quiet(true)
                    .build();

            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(deletePayload)
                    .build();

            s3Client.deleteObjects(deleteRequest);
        } catch (S3Exception e) {
            throw new StorageException("S3 batch delete failed", ErrorCode.STORAGE_ERROR, e);
        }
    }

    @Override
    public String getPublicUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    @Override
    public String extractKey(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return null;
        }

        try {
            URL url = new URI(publicUrl).toURL();
            String host = url.getHost();
            String path = url.getPath();

            String expectedHost = String.format("%s.s3.%s.amazonaws.com", bucketName, region);

            if (!expectedHost.equalsIgnoreCase(host)) {
                throw new StorageException("URL host mismatch. Expected: " + expectedHost, ErrorCode.STORAGE_VALIDATION_ERROR);
            }

            if (path.startsWith("/")) {
                return path.substring(1);
            }

            throw new StorageException("Invalid S3 path format.", ErrorCode.STORAGE_VALIDATION_ERROR);

        } catch (Exception e) {
            throw new StorageException("Invalid icon URL format: " + publicUrl, ErrorCode.STORAGE_VALIDATION_ERROR, e);
        }
    }

    /**
     * @param uploadRequest
     * @return
     */
    @Override
    public TransactionReceiptUploadResponse generateUploadUrl(TransactionReceiptUploadRequest uploadRequest) {

        validateFile(uploadRequest.fileName(), uploadRequest.fileType());

        String key = createFileName(uploadRequest.fileName());

        return null;
    }

    private String createFileName(String fileName) {
        return null;
    }

    private void validateFile(String fileName, String fileType) {

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new StorageException("File name must be provided", ErrorCode.STORAGE_VALIDATION_ERROR);
        }

        if (fileType == null || fileType.trim().isEmpty()) {
            throw new StorageException("File type must be provided", ErrorCode.STORAGE_VALIDATION_ERROR);
        }

        checkExtensionConflict(fileName, fileType);
    }

    private void checkExtensionConflict(String fileName, String fileType) {

        String extension = extractExtension(fileName);

        if (extension == null) {
            throw new StorageException("file extension not provided", ErrorCode.STORAGE_VALIDATION_ERROR);
        }

        if (!ALLOWED_EXTENSIONS_MAP.containsKey(extension)) {
            throw new StorageException("Invalid file extension. Allowed extensions: " + ALLOWED_EXTENSIONS_MAP.values(), ErrorCode.STORAGE_VALIDATION_ERROR);
        }

        String expectedFileType = ALLOWED_EXTENSIONS_MAP.get(extension);
        if (!expectedFileType.equals(fileType)) {
            throw new StorageException(
                    String.format("File type conflict. For file extension .%s, expected %s, but received %s",
                            extension, expectedFileType, fileType),
                    ErrorCode.STORAGE_VALIDATION_ERROR
            );
        }
    }

    @Override
    public boolean exists(String key) {
        if (key == null) {
            return false;
        }

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty", ErrorCode.STORAGE_VALIDATION_ERROR);
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new StorageException("File exceeds max size of " + MAX_FILE_SIZE_MB + "MB", ErrorCode.STORAGE_VALIDATION_ERROR);
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new StorageException("Invalid file type. Allowed types: " + ALLOWED_CONTENT_TYPES, ErrorCode.STORAGE_VALIDATION_ERROR);
        }
    }

    private String extractExtension(String originalFilename) {
        return StringUtils.getFilenameExtension(originalFilename);
    }


    private String generateKey(String pathPrefix, Long resourceId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%d/%s.%s", pathPrefix, resourceId, uuid, extension);
    }

}
