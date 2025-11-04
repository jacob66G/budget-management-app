package com.example.budget_management_app.category.dto;

import jakarta.validation.constraints.Size;

public record CategoryUpdateRequestDto(
    @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
    String name,
    String type,
    String iconKey
) {}
