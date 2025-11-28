package com.example.budget_management_app.common.service;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

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
