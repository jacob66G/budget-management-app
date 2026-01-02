package com.example.budget_management_app.common.storage.service;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    @Value("${aws.region}")
    private String region;

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
    public String generatePresignedPutUrl(String key, String contentType, Long fileSize) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength(fileSize)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    /**
     * @param key
     * @return
     */
    @Override
    public String generatePresignedGetUrl(String key, Long validFor) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(validFor))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    public boolean exists(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            throw new StorageException("S3 error", ErrorCode.STORAGE_ERROR);
        }
    }
}
