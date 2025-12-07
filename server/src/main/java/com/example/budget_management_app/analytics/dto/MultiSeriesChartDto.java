package com.example.budget_management_app.analytics.dto;

import java.time.LocalDate;
import java.util.List;

public record MultiSeriesChartDto(
        List<LocalDate> dates,
        List<ChartSeriesDto> series
) {
}
