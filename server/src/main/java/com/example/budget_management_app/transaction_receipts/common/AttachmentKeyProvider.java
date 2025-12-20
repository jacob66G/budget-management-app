package com.example.budget_management_app.transaction_receipts.common;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class AttachmentKeyProvider {

    private static final String KEY_PATTERN_STRING = "receipts/users/{userId}/transactions/{transactionId}/{fileName}.{extension}";
    private final UriTemplate keyTemplate;

    public AttachmentKeyProvider() {
        this.keyTemplate = new UriTemplate(KEY_PATTERN_STRING);
    }

    public String generateKey(Long transactionId, Long userId, String extension) {

        // new file name to be stored in S3
        String uuid = UUID.randomUUID().toString();

        // expected key receipts/users/{id}/transactions/{id}/{uuid}.{extension}
        return keyTemplate.expand(userId, transactionId, uuid, extension).toString();
    }


    public void validateKeyStructure(Long transactionId, Long userId, String key) {

        if (!keyTemplate.matches(key)) {
            log.error("Received key format is invalid: {}", key);
            throw new IllegalArgumentException("Key format is invalid");
        }

        Map<String, String> metadata = keyTemplate.match(key);
        Long keyUserId = Long.valueOf(metadata.get("userId"));
        Long keyTransactionId = Long.valueOf(metadata.get("transactionId"));

        if (!keyUserId.equals(userId)) {
            throw new NotFoundException(User.class.getSimpleName(), keyUserId, ErrorCode.NOT_FOUND);
        }

        if (!keyTransactionId.equals(transactionId)) {
            throw new NotFoundException(Transaction.class.getSimpleName(), keyTransactionId, ErrorCode.NOT_FOUND);
        }
    }
}
