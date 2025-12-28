package com.example.budget_management_app.transaction_receipts.service;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.StorageException;
import com.example.budget_management_app.common.storage.service.S3StorageService;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction.service.TransactionService;
import com.example.budget_management_app.transaction_receipts.utils.AttachmentKeyProvider;
import com.example.budget_management_app.transaction_receipts.utils.AttachmentValidator;
import com.example.budget_management_app.transaction_receipts.domain.TransactionPhoto;
import com.example.budget_management_app.transaction_receipts.dto.AttachmentViewResponse;
import com.example.budget_management_app.transaction_receipts.dto.ConfirmAttachmentUploadRequest;
import com.example.budget_management_app.transaction_receipts.dto.ReceiptUploadUrlRequest;
import com.example.budget_management_app.transaction_receipts.dto.ReceiptUploadUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionAttachmentServiceImpl implements TransactionAttachmentService {

    private final S3StorageService storageService;
    private final TransactionService transactionService;
    private final AttachmentValidator attachmentValidator;
    private final AttachmentKeyProvider attachmentKeyProvider;

    /**
     * @param uploadReq
     * @return
     */
    @Override
    public ReceiptUploadUrlResponse getPresignedPutUrl(
            Long transactionId,
            ReceiptUploadUrlRequest uploadReq,
            Long userId
    ) {

        // validating if transaction exists and belong to user
        this.validateTransactionAccess(transactionId, userId);

        String fileType = uploadReq.fileType();
        Long fileSize = uploadReq.fileSize();

        // validating file data and getting file extension
        String extension = this.attachmentValidator.validateFileAndGetExtension(uploadReq.fileName(), fileType, fileSize);

        // generating key for receipt
        String key = this.attachmentKeyProvider.generateKey(transactionId, userId, extension);

        // getting presigned url
        return ReceiptUploadUrlResponse.of(storageService.generatePresignedPutUrl(key, fileType, fileSize), key);
    }

    /**
     * @param transactionId
     * @param attachFileReq
     * @param userId
     * @return
     */
    @Transactional
    @Override
    public AttachmentViewResponse confirmAttachmentUpload(Long transactionId, ConfirmAttachmentUploadRequest attachFileReq, Long userId) {

        String s3Key = attachFileReq.key();
        String originalFileName = attachFileReq.originalFileName();

        // validate user and transaction data from received key
        this.attachmentKeyProvider.validateKeyStructure(transactionId, userId, s3Key);

        // checking if key already exists
        this.checkIfKeyExistsInS3(s3Key);

        // checking if transaction exist and belong to given user
        Transaction transaction = transactionService.findByIdAndUserId(transactionId, userId);
        TransactionPhoto existingPhoto = transaction.getTransactionPhoto();

        if (existingPhoto != null) {
            if (existingPhoto.getKey().equals(s3Key)) {
                return generateViewResponse(s3Key, attachFileReq.originalFileName());
            }
            try {
                this.storageService.delete(existingPhoto.getKey());
            } catch (S3Exception e) {
                log.warn("Warning: Error occurred when deleting old file: {}. Error: {}", existingPhoto.getKey(), e.getMessage());
            }

            existingPhoto.setKey(s3Key);
            existingPhoto.setOriginalFileName(originalFileName);

        } else {
            TransactionPhoto photo = new TransactionPhoto(originalFileName, s3Key);
            transaction.setTransactionPhoto(photo);
        }
        return generateViewResponse(s3Key, attachFileReq.originalFileName());
    }

    private AttachmentViewResponse generateViewResponse(String key, String fileName) {

        log.info("Generating presigned get url for key: {}", key);
        long validForMinutes = 60L;
        String presignedUrl = this.storageService.generatePresignedGetUrl(key, validForMinutes);

        return new AttachmentViewResponse(
                fileName,
                presignedUrl,
                LocalDateTime.now().plusMinutes(validForMinutes)
        );
    }

    /**
     * @param transactionId
     * @param userId
     * @return
     */
    @Override
    public AttachmentViewResponse getPresignedGetUrl(Long transactionId, Long userId) {

        Transaction transaction = this.transactionService.findByIdAndUserId(transactionId, userId);
        TransactionPhoto transactionPhoto = transaction.getTransactionPhoto();

        if (transactionPhoto == null) {
            log.info("Transaction attachment for transaction with id: {} does not exist.", transactionId);
            throw new NotFoundException("Transaction photo for transaction with id: " + transactionId + " not found", ErrorCode.NOT_FOUND);
        }
        return this.generateViewResponse(transactionPhoto.getKey(), transactionPhoto.getOriginalFileName());
    }

    private void validateTransactionAccess(Long transactionId, Long userId) {
        if (!this.transactionService.existsByIdAndUserId(transactionId, userId)) {
            log.info("Transaction with id: {} does not exist or do not belong to user with id: {}", transactionId, userId);
            throw new NotFoundException(Transaction.class.getSimpleName(), transactionId, ErrorCode.NOT_FOUND);
        }
    }

    private void checkIfKeyExistsInS3(String key) {
        if (!this.storageService.exists(key)) {
            log.info("Received key does not exist in S3 bucket: {}", key);
            throw new StorageException("Key does not exist.", ErrorCode.STORAGE_ERROR);
        }
    }
}
