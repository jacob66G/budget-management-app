package com.example.budget_management_app.transaction_receipts.dto;

public record ReceiptUploadUrlRequest(
        String fileName,
        String fileType,
        Long fileSize
) {
}
