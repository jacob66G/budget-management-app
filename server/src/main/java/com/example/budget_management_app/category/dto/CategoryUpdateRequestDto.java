package com.example.budget_management_app.category.dto;

public record CategoryUpdateRequestDto(
    String name,
    String type,
    String iconPath
) {}
