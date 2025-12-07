export interface ChartPoint {
    date: string,
    amount: number
}

export interface CategoryChartPoint {
    categoryName: string,
    amount: number
}

export interface CashFlowChartPoint {
    date: string,
    totalIncome: number,
    totalExpense: number
}

export interface ChartSeries {
    label: string; 
    data: number[];
}

export interface MultiSeriesChart {
    dates: string[];
    series: ChartSeries[];
}