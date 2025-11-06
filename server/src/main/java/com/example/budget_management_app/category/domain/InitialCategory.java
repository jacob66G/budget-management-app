package com.example.budget_management_app.category.domain;

import lombok.Getter;

@Getter
public enum InitialCategory {
    // === INCOME ===
    SALARY("Salary", CategoryType.INCOME, "categories/loan-icon.png", false),
    BONUS("Bonus", CategoryType.INCOME, "categories/gift-hand-present-icon", false),
    INVESTMENT_PROFIT("Investment Profit", CategoryType.INCOME, "categories/investment-analysis-icon.png", false),
    GIFT_RECEIVED("Gift Received", CategoryType.INCOME, "categories/present-icon.png", false),

    // === EXPENSE ===
    FOOD("Food & Groceries", CategoryType.EXPENSE, "categories/burger-icon.png", false),
    TRANSPORT("Transport", CategoryType.EXPENSE, "categories/car-icon.png", false),
    HOUSING("Housing & Rent", CategoryType.EXPENSE, "categories/house-icon.png", false),
    UTILITIES("Utilities & Bills", CategoryType.EXPENSE, "categories/payday-icon.png", false),
    HEALTH("Health & Medicine", CategoryType.EXPENSE, "categories/heart-icon.png", false),
    EDUCATION("Education", CategoryType.EXPENSE, "categories/graduation-cap-icon.png", false),
    ENTERTAINMENT("Entertainment", CategoryType.EXPENSE, "categories/concert-icon", false),
    CLOTHING("Clothing & Accessories", CategoryType.EXPENSE, "categories/shirt-icon.png", false),
    SUBSCRIPTIONS("Subscriptions", CategoryType.EXPENSE, "categories/four-squares-line-icon.png", false),
    INSURANCE("Insurance", CategoryType.EXPENSE, "categories/shield-checkmark-black-icon.png", false),

    // === GENERAL ===
    SAVINGS("Savings", CategoryType.GENERAL, "categories/piggy-saving-icon.png", false),
    INVESTMENTS("Investments", CategoryType.GENERAL, "categories/money-bag-icon.png", false),
    DEBT("Debt & Loans", CategoryType.GENERAL, "categories/house-hand-mortgage-icon.png", false),
    UNCATEGORIZED("Uncategorized", CategoryType.GENERAL, "categories/four-squares-line-icon.png", false),

    // === DEFAULT ===
    OTHER_GENERAL("Other", CategoryType.GENERAL, "categories/question-mark-icon.png", true);

    private final String name;
    private final CategoryType type;
    private final String iconKey;
    private final boolean isDefault;

    InitialCategory(String name, CategoryType type, String iconKey, boolean isDefault) {
        this.name = name;
        this.type = type;
        this.iconKey = iconKey;
        this.isDefault = isDefault;
    }
}
