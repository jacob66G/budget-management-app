package com.example.budget_management_app.common.service;

public interface S3PathValidator {

    boolean isValidPathForCategory(String urlString);

    boolean isValidPathForAccount(String urlString);
}
