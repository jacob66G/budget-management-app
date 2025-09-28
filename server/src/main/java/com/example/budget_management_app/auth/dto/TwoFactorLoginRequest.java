package com.example.budget_management_app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TwoFactorLoginRequest(
        @NotNull
        Long userId,
        @NotBlank(message = "Code is required")
        @Size(min = 6, max = 6, message = "Code must contain 6 numbers")
        String code
) {
}
