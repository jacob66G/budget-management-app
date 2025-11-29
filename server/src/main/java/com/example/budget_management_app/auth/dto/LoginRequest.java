package com.example.budget_management_app.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email should be valid")
        String email,
        @NotBlank(message = "Password cannot be empty")
        String password
) {
}
