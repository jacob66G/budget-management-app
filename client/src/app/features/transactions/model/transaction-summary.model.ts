import { TransactionType } from "../constants/transaction-type.enum";
import { AccountSummary } from "./account-summary.model";
import { CategorySummary } from "./category-summary.model";

export interface TransactionSummary {
    id: number,
    title: string,
    amount: number,
    type: TransactionType,
    description: string,
    transactionDate: Date,
    account: AccountSummary,
    category: CategorySummary,
    recurringTransactionId: number | undefined,
    hasAttachment: boolean
}