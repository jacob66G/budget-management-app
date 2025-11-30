package com.example.budget_management_app.analytics.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record DateRange(
        @NotNull(message = "The start date (from) cannot be empty.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,
        @NotNull(message = "The end date (to) cannot be empty.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) {
    @AssertTrue(message = "The start date (from) must be earlier than the end date (up to).")
    private boolean isDateRangeValid() {
        if (from == null || to == null) {
            return true;
        }
        return from.isBefore(to);
    }

    public LocalDateTime getFromDateTime() {
        if (from == null) return null;
        return from.atStartOfDay();
    }

    public LocalDateTime getToDateTime() {
        if (to == null) return null;
        return to.atTime(LocalTime.MAX);
    }
}
