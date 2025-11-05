package com.example.budget_management_app.category.domain;

import com.example.budget_management_app.transaction.domain.TransactionType;

public enum CategoryType {
    INCOME {
        @Override
        public boolean supports(TransactionType transactionType) {
            return transactionType == TransactionType.INCOME;
        }
    },

    EXPENSE {
        @Override
        public boolean supports(TransactionType transactionType) {
            return transactionType == TransactionType.EXPENSE;
        }
    },

    GENERAL {
        @Override
        public boolean supports(TransactionType transactionType) {
            return true;
        }
    };

    public abstract boolean supports(TransactionType transactionType);
}
