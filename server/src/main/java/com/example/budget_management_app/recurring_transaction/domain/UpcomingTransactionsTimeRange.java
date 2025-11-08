package com.example.budget_management_app.recurring_transaction.domain;

import java.time.LocalDate;

public enum UpcomingTransactionsTimeRange {
    NEXT_7_DAYS {
        @Override
        public LocalDate calculateEndDate(LocalDate fromDate) {
            return fromDate.plusDays(7);
        }
    },
    NEXT_14_DAYS {
        @Override
        public LocalDate calculateEndDate(LocalDate fromDate) {
            return fromDate.plusDays(14);
        }
    },
    NEXT_MONTH {
        @Override
        public LocalDate calculateEndDate(LocalDate fromDate) {
            return fromDate.plusMonths(1);
        }
    };

    public abstract LocalDate calculateEndDate(LocalDate fromDate);
}
