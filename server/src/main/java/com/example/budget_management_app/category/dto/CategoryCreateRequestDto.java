package com.example.budget_management_app.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequestDto(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Type is required")
        String type,
        String iconPath
) {
}
