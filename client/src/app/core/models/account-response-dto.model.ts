export interface AccountResponseDto {
    id: number,
    type: string,
    name: string,
    balance: number,
    totalIncome: number,
    totalExpense: number,
    currency: string,
    isDefault: boolean,
    iconKey: string,
    includeTotalBalance: boolean,
    createdAt: Date,
    status: string
}