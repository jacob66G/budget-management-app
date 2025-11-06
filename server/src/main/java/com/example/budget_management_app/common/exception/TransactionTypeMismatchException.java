package com.example.budget_management_app.common.exception;

import com.example.budget_management_app.category.domain.CategoryType;
import com.example.budget_management_app.transaction_common.domain.TransactionType;

public class TransactionTypeMismatchException extends ApplicationException{

    public TransactionTypeMismatchException(TransactionType transactionType,
                                            CategoryType categoryType,
                                            ErrorCode errorCode) {
        super("Transaction type: " + transactionType.name() + " mismatch category type: " + categoryType.name(), errorCode);
    }
}
