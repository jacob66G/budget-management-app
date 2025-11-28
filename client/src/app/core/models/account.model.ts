export interface Account {
    id: number,
    type: string,
    name: string,
    balance: number,
    totalIncome: number,
    totalExpense:  number,
    currency: string,
    isDefault: boolean,
    iconPath?: string,
    includeInTotalBalance: boolean,
    createdAt: string,
    status: string
}