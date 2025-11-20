import { TransactionType } from "../constants/transaction-type.enum";

export interface TransactionCreateRequest {
    amount: number,
    title: string,
    type: TransactionType,
    description: string,
    accountId: number,
    categoryId: number
}