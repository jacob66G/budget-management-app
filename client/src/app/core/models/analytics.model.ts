export interface ChartPoint {
    date: Date,
    amount: number
}

export interface CategoryChartPoint {
    categoryName: string,
    amount: number
}

export interface CashFlowChartPoint {
    date: Date,
    totalIncome: number,
    totalExpense: number
}