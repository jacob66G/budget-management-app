package com.example.budget_management_app.transaction_receipts.dto;

public record ReceiptUploadUrlResponse(
        String url,
        String key
) {

    public static ReceiptUploadUrlResponse of(String url, String key) {
        return new ReceiptUploadUrlResponse(url, key);
    }
}
