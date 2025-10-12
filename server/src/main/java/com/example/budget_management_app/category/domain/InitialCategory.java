package com.example.budget_management_app.category.domain;

import lombok.Getter;

@Getter
public enum InitialCategory {
    // === INCOME ===
    SALARY("Salary", CategoryType.INCOME, "", false),
    BONUS("Bonus", CategoryType.INCOME, "", false),
    INVESTMENT_PROFIT("Investment Profit", CategoryType.INCOME, "", false),
    GIFT_RECEIVED("Gift Received", CategoryType.INCOME, "", false),

    // === EXPENSE ===
    FOOD("Food & Groceries", CategoryType.EXPENSE, "", false),
    TRANSPORT("Transport", CategoryType.EXPENSE, "", false),
    HOUSING("Housing & Rent", CategoryType.EXPENSE, "", false),
    UTILITIES("Utilities & Bills", CategoryType.EXPENSE, "", false),
    HEALTH("Health & Medicine", CategoryType.EXPENSE, "", false),
    EDUCATION("Education", CategoryType.EXPENSE, "", false),
    ENTERTAINMENT("Entertainment", CategoryType.EXPENSE, "", false),
    CLOTHING("Clothing & Accessories", CategoryType.EXPENSE, "", false),
    SUBSCRIPTIONS("Subscriptions", CategoryType.EXPENSE, "", false),
    INSURANCE("Insurance", CategoryType.EXPENSE, "", false),

    // === GENERAL ===
    SAVINGS("Savings", CategoryType.GENERAL, "", false),
    INVESTMENTS("Investments", CategoryType.GENERAL, "", false),
    DEBT("Debt & Loans", CategoryType.GENERAL, "", false),
    UNCATEGORIZED("Uncategorized", CategoryType.GENERAL, "", false),

    // === DEFAULT ===
    OTHER_GENERAL("Other", CategoryType.GENERAL, "", true),
    OTHER_INCOME("Other", CategoryType.INCOME, "", true),
    OTHER_EXPENSE("Other", CategoryType.EXPENSE, "", true);

    private final String name;
    private final CategoryType type;
    private final String iconPath;
    private final boolean isDefault;

    InitialCategory(String name, CategoryType type, String iconPath, boolean isDefault) {
        this.name = name;
        this.type = type;
        this.iconPath = iconPath;
        this.isDefault = isDefault;
    }
}
