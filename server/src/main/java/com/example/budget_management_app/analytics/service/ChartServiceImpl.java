package com.example.budget_management_app.analytics.service;

import com.example.budget_management_app.analytics.dto.BalanceHistoryPointDto;
import com.example.budget_management_app.analytics.dto.CashFlowPointDto;
import com.example.budget_management_app.analytics.dto.CategoryBreakdownPointDto;
import com.example.budget_management_app.common.exception.InternalException;
import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class ChartServiceImpl implements ChartService {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color GRID_COLOR = new Color(240, 240, 240);
    private static final Color INCOME_COLOR = new Color(76, 175, 80);
    private static final Color EXPENSE_COLOR = new Color(244, 67, 54);

    private static final Color[] SERIES_COLORS = new Color[]{
            new Color(63, 81, 181), new Color(255, 64, 129),
            new Color(0, 150, 136), new Color(255, 193, 7),
            new Color(156, 39, 176), new Color(244, 67, 54)
    };

    private static final Color[] CATEGORY_COLORS = new Color[]{
            new Color(63, 81, 181), new Color(255, 64, 129), new Color(255, 193, 7),
            new Color(0, 150, 136), new Color(156, 39, 176), new Color(244, 67, 54),
            new Color(76, 175, 80), new Color(121, 85, 72), new Color(96, 125, 139),
            new Color(33, 150, 243)
    };

    @Override
    public byte[] generateBalanceChart(List<BalanceHistoryPointDto> data) {
        if (data == null || data.isEmpty()) return null;

        XYChart chart = new XYChartBuilder().width(WIDTH).height(HEIGHT).title("").build();
        applyBaseStyle(chart);

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideN);
        chart.getStyler().setPlotGridHorizontalLinesVisible(true);
        chart.getStyler().setPlotGridVerticalLinesVisible(false);
        chart.getStyler().setPlotGridLinesColor(GRID_COLOR);
        chart.getStyler().setMarkerSize(5);

        List<LocalDate> dates = data.stream()
                .map(BalanceHistoryPointDto::date)
                .toList();

        configureDateAxis(chart, dates);

        List<Date> xData = dates.stream()
                .map(this::toLegacyDate)
                .toList();


        List<Double> yData = data.stream()
                .map(dto -> dto.amount().doubleValue())
                .toList();

        XYSeries series = chart.addSeries("Balance", xData, yData);

        series.setLineColor(SERIES_COLORS[0]);
        series.setMarkerColor(SERIES_COLORS[0]);
        series.setMarker(SeriesMarkers.CIRCLE);
        series.setLineStyle(SeriesLines.SOLID);

        return renderToPng(chart);
    }

    @Override
    public byte[] generateCashFlowChart(List<CashFlowPointDto> data) {
        if (data == null || data.isEmpty()) return null;

        CategoryChart chart = new CategoryChartBuilder().width(WIDTH).height(HEIGHT).title("").build();
        applyBaseStyle(chart);

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
        chart.getStyler().setPlotGridHorizontalLinesVisible(true);
        chart.getStyler().setPlotGridVerticalLinesVisible(false);
        chart.getStyler().setPlotGridLinesColor(GRID_COLOR);
        chart.getStyler().setAvailableSpaceFill(0.8);
        chart.getStyler().setOverlapped(false);
        chart.getStyler().setXAxisLabelRotation(45);

        DateTimeFormatter formatter = getFormatter(
                data.getFirst().date(),
                data.getLast().date()
        );

        int totalDataPoints = data.size();
        int maxLabels = 15;
        int step = 1;

        if (totalDataPoints > maxLabels) {
            step = (totalDataPoints / maxLabels) + 1;
        }

        List<String> xData = new ArrayList<>();
        List<BigDecimal> incomeData = new ArrayList<>();
        List<BigDecimal> expenseData = new ArrayList<>();

        for (int i = 0; i < totalDataPoints; i++) {
            CashFlowPointDto item = data.get(i);
            if (i % step == 0) {
                xData.add(item.date().format(formatter));
            } else {
                xData.add(" ");
            }

            incomeData.add(item.totalIncome());
            expenseData.add(item.totalExpense());
        }

        addBarSeries(chart, "Income", xData, incomeData, INCOME_COLOR);
        addBarSeries(chart, "Expense", xData, expenseData, EXPENSE_COLOR);

        return renderToPng(chart);
    }

    @Override
    public byte[] generateCategorySumChart(List<CategoryBreakdownPointDto> data) {
        if (data == null || data.isEmpty()) return null;

        PieChart chart = new PieChartBuilder().width(WIDTH).height(HEIGHT).title("").build();
        applyBaseStyle(chart);

        chart.getStyler().setDefaultSeriesRenderStyle(PieSeries.PieSeriesRenderStyle.Donut);
        chart.getStyler().setDonutThickness(0.4);
        chart.getStyler().setPlotContentSize(0.9);
        chart.getStyler().setPlotBackgroundColor(BACKGROUND_COLOR);
        chart.getStyler().setLabelsVisible(false);

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setLegendPadding(10);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Vertical);

        for (int i = 0; i < data.size(); i++) {
            CategoryBreakdownPointDto point = data.get(i);
            PieSeries series = chart.addSeries(point.categoryName(), point.amount());
            series.setFillColor(CATEGORY_COLORS[i % CATEGORY_COLORS.length]);
        }

        return renderToPng(chart);
    }

    private void applyBaseStyle(Chart<?, ?> chart) {
        chart.getStyler().setChartBackgroundColor(BACKGROUND_COLOR);
        chart.getStyler().setChartTitleVisible(false);
        chart.getStyler().setLegendVisible(true);
    }


    private byte[] renderToPng(Chart<?, ?> chart) {
        try {
            return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new InternalException("Unexpected error occurred", e);
        }
    }


    private void addBarSeries(CategoryChart chart, String name, List<String> xData, List<? extends Number> yData, Color color) {
        CategorySeries series = chart.addSeries(name, xData, yData);
        series.setFillColor(color);
        series.setLineColor(color);
    }

    private String determineDatePattern(LocalDate start, LocalDate end) {
        long diffDays = ChronoUnit.DAYS.between(start, end);
        return (diffDays > 90) ? "MMM yyyy" : "dd.MM";
    }

    private void configureDateAxis(XYChart chart, List<LocalDate> dates) {
        if (dates == null || dates.size() < 2) return;

        String pattern = determineDatePattern(dates.getFirst(), dates.getLast());

        chart.getStyler().setDatePattern(pattern);
        chart.getStyler().setLocale(Locale.ENGLISH);
        chart.getStyler().setXAxisLabelRotation(45);
    }

    private DateTimeFormatter getFormatter(LocalDate start, LocalDate end) {
        String pattern = determineDatePattern(start, end);
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
    }

    private Date toLegacyDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
