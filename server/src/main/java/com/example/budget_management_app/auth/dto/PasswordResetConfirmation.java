package com.example.budget_management_app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmation(
        String token,
        @NotBlank(message = "New password cannot be empty")
        @Size(min = 5, message = "Password must contain at least 5 characters")
        String newPassword,
        @NotBlank(message = "The new password confirmation cannot be empty")
        String confirmedNewPassword
) {
}
