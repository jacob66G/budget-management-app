package com.example.budget_management_app.transaction_receipts.dto;

public record ConfirmAttachmentUploadRequest(
        String originalFileName,
        String key
) {
}
