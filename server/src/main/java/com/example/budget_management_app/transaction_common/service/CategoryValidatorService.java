package com.example.budget_management_app.transaction_common.service;

import com.example.budget_management_app.category.domain.CategoryType;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.TransactionTypeMismatchException;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import org.springframework.stereotype.Service;

@Service
public class CategoryValidatorService {

    public void validateCategoryType(CategoryType categoryType, TransactionType transactionType) {
        if (!categoryType.supports(transactionType)) {
            throw new TransactionTypeMismatchException(transactionType, categoryType, ErrorCode.TRANSACTION_TYPE_MISMATCH);
        }
    }
}
