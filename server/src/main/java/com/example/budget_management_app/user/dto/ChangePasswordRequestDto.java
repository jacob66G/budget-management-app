package com.example.budget_management_app.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank(message = "Old password cannot be empty")
        String oldPassword,
        @NotBlank(message = "New password cannot be empty")
        @Size(min = 5, message = "Password must contain at least 5 characters")
        String newPassword,
        @NotBlank(message = "The new password confirmation cannot be empty")
        String confirmedNewPassword
) {
}