export interface FinancialSummaryResponse {
    closingBalance : number,
    totalIncome: number,
    totalExpense: number,
    netSavings: number,
    accounts: AccountSummary[],
    recentTransactions: TransactionSummary[]
}

export interface AccountSummary {
    id: number,
    name: string,
    balance: number,
    currency: string,
    type: string,
    includeInTotalBalance: boolean,
    iconPath: string
}

export interface TransactionSummary {
    title: string,
    amount: number,
    type: string,
    date: string,
    categoryName: string,
    categoryIconPath: string
    accountName: string
}