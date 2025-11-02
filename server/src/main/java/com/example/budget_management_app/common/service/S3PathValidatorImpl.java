package com.example.budget_management_app.common.service;

import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Service
public class S3PathValidatorImpl implements S3PathValidator {

    @Override
    public boolean isValidPathForCategory(String urlString) {
        return isValidPathWithPrefix(urlString, "/categories/");
    }

    @Override
    public boolean isValidPathForAccount(String urlString) {
        return isValidPathWithPrefix(urlString, "/accounts/");
    }

    private boolean isValidPathWithPrefix(String urlString, String requiredPrefix) {
        if (urlString == null || urlString.isEmpty()) {
            return false;
        }

        try {
            URL url = URI.create(urlString).toURL();
            String path = url.getPath();

            if (!url.getHost().endsWith(".amazonaws.com")) {
                return false;
            }

            if (!path.contains(requiredPrefix)) {
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
