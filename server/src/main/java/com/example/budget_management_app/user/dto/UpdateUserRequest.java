package com.example.budget_management_app.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
        String name,
        @Size(min = 2, max = 30, message = "Surname must be between 2 and 30 characters")
        String surname
) {
}
