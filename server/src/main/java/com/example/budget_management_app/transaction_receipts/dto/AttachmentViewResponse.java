package com.example.budget_management_app.transaction_receipts.dto;

import java.time.LocalDateTime;

public record AttachmentViewResponse(
        String originalFileName,
        String downloadUrl,
        LocalDateTime expiresAt
) {
}
