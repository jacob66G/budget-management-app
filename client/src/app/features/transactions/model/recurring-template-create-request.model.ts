import { RecurringInterval } from "../constants/recurring-interval.enum";
import { TransactionType } from "../constants/transaction-type.enum";

export interface RecurringTransactionCreateRequest {
    amount: number,
    title: string,
    type: TransactionType,
    description: string,
    startDate: Date,
    recurringInterval: RecurringInterval,
    recurringValue: number,
    accountId: number,
    categoryId: number
}