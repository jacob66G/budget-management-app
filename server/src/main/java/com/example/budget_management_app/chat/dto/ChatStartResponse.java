package com.example.budget_management_app.chat.dto;

public record ChatStartResponse(
        String chatId,
        String message,
        String description
) {
}
