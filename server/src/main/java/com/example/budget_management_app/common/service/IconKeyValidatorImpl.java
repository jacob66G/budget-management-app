package com.example.budget_management_app.common.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class IconKeyValidatorImpl implements IconKeyValidator {

    private static final Set<String> ALLOWED_CATEGORY_KEYS = Set.of(
            "categories/salary.png",
            "categories/bonus.png",
            "categories/food.png",
            "categories/transport.png",
            "categories/housing.png",
            "categories/bills.png",
            "categories/health.png",
            "categories/entertainment.png",
            "categories/savings.png",
            "categories/other.png"
    );

    private static final Set<String> ALLOWED_ACCOUNT_KEYS = Set.of(
            "accounts/wallet.png",
            "accounts/savings.png",
            "accounts/dollar.png",
            "accounts/card.png",
            "accounts/credit.png",
            "accounts/euro.png",
            "accounts/holidays.png"
    );

    public boolean isValidCategoryIconKey(String key) {
        return ALLOWED_CATEGORY_KEYS.contains(key);
    }

    public boolean isValidAccountIconKey(String key) {
        return ALLOWED_ACCOUNT_KEYS.contains(key);
    }

}
