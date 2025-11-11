package com.example.budget_management_app.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequestDto(
        @NotBlank(message = "Name cannot be empty")
        @Size(min = 2, max=30, message = "Name must be between 2 and 30 characters")
        String name,
        @NotBlank(message = "Surname cannot be empty")
        @Size(min = 2, max=30, message = "Surname must be between 2 and 30 characters")
        String surname,
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email should be valid")
        String email,
        @NotBlank(message = "Password cannot be empty")
        @Size(min = 5, message = "Password must contain at least 5 characters")
        String password,
        @NotBlank(message = "The new password confirmation cannot be empty")
        String passwordConfirmation
) {
}
