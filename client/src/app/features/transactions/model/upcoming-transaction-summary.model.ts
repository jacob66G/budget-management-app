import { TransactionType } from "../constants/transaction-type.enum";
import { AccountSummary } from "./account-summary.model";
import { CategorySummary } from "./category-summary.model";

export interface UpcomingTransactionSummary {
    recurringTemplateId: number,
    amount: number;
    title: string,
    type: TransactionType,
    nextOccurrence: Date,
    accountSummary: AccountSummary,
    categorySummary: CategorySummary
}