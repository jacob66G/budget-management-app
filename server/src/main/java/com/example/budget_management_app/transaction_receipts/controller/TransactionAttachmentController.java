package com.example.budget_management_app.transaction_receipts.controller;

import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction_receipts.dto.ConfirmAttachmentUploadRequest;
import com.example.budget_management_app.transaction_receipts.dto.AttachmentViewResponse;
import com.example.budget_management_app.transaction_receipts.dto.ReceiptUploadUrlRequest;
import com.example.budget_management_app.transaction_receipts.dto.ReceiptUploadUrlResponse;
import com.example.budget_management_app.transaction_receipts.service.TransactionAttachmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping(path = "/api/v1/transactions/{id}/attachment", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class TransactionAttachmentController {

    private final TransactionAttachmentService transactionReceiptService;

    @PostMapping("/presigned-upload-url")
    public ResponseEntity<ReceiptUploadUrlResponse> getPresignedPutUrl(
            @Positive(message = "The transaction id value must be positive") @PathVariable Long id,
            @Valid @RequestBody ReceiptUploadUrlRequest uploadReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        ReceiptUploadUrlResponse uploadRes =
                this.transactionReceiptService.getPresignedPutUrl(
                        id,
                        uploadReq,
                        userDetails.getId()
                );

        return ResponseEntity.ok(uploadRes);
    }

    @PostMapping
    public ResponseEntity<AttachmentViewResponse> confirmAttachmentUpload(
            @Positive(message = "The transaction id value must be positive") @PathVariable Long id,
            @Valid @RequestBody ConfirmAttachmentUploadRequest attachFileReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.transactionReceiptService.confirmAttachmentUpload(id, attachFileReq, userDetails.getId()));
    }

    @GetMapping
    public ResponseEntity<AttachmentViewResponse> getPresignedGetUrl(
            @Positive (message = "The transaction id value must be positive") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(this.transactionReceiptService.getPresignedGetUrl(id, userDetails.getId()));
    }
}
