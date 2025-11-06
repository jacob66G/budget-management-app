package com.example.budget_management_app.common.service;

import com.example.budget_management_app.config.IconConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IconKeyValidatorImpl implements IconKeyValidator {

    private final IconConfig iconConfig;

    public boolean isValidCategoryIconKey(String key) {
        return iconConfig.getCategories().contains(key);
    }

    public boolean isValidAccountIconKey(String key) {
        return iconConfig.getAccounts().contains(key);
    }

}
