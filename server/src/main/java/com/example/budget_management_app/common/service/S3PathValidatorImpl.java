package com.example.budget_management_app.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Service
public class S3PathValidatorImpl implements S3PathValidator {

    private final String expectedS3Host;

    public S3PathValidatorImpl(
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.region}") String region
    ) {
        this.expectedS3Host = String.format("%s.s3.%s.amazonaws.com", bucketName, region);
    }

    @Override
    public boolean isValidPathForCategory(String urlString) {
        return isValidPathWithPrefix(urlString, "categories/");
    }

    @Override
    public boolean isValidPathForAccount(String urlString) {
        return isValidPathWithPrefix(urlString, "accounts/");
    }

    private boolean isValidPathWithPrefix(String urlString, String requiredDirectory) {
        if (urlString == null || urlString.isEmpty()) {
            return false;
        }

        try {
            URL url = URI.create(urlString).toURL();
            String path = url.getPath();

            if (!expectedS3Host.equalsIgnoreCase(url.getHost())) {
                return false;
            }

            String fullPathPrefix = "/" + requiredDirectory;
            if (!path.startsWith(fullPathPrefix)) {
                return false;
            }

            String filename = path.substring(fullPathPrefix.length());

            if (filename.isEmpty()) {
                return false;
            }

            if (filename.contains("/")) {
                return false;
            }

            if (!path.toLowerCase().endsWith(".png") && !path.toLowerCase().endsWith(".jpg")) {
                return false;
            }

            return true;
        } catch (IllegalArgumentException | MalformedURLException e) {
            return false;
        }
    }
}
