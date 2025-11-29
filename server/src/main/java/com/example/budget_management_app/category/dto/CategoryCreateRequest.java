package com.example.budget_management_app.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
        String name,
        @NotBlank(message = "Type is required")
        String type,
        String iconPath
) {
}
