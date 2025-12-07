import { RecurringInterval } from "../constants/recurring-interval.enum";
import { TransactionType } from "../constants/transaction-type.enum";
import { AccountSummary } from "./account-summary.model";
import { CategorySummary } from "./category-summary.model";

export interface RecurringTemplateSummary {
    id: number,
    title: string,
    amount: number,
    type: TransactionType,
    isActive: boolean,
    description: string,
    nextOccurrence: Date,
    recurringInterval: RecurringInterval,
    recurringValue: number,
    accountSummary: AccountSummary,
    categorySummary: CategorySummary
}