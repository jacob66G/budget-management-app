package com.example.budget_management_app.category.dto;

import com.example.budget_management_app.category.domain.CategoryType;

public record CategoryResponseDto(
        Long id,
        String name,
        CategoryType type,
        Boolean isDefault,
        String iconKey
) {
}
