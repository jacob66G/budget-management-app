package com.example.budget_management_app.transaction.dto;

public record TransactionReceiptUploadRequest(
        String fileName,
        String fileType
) {
}
