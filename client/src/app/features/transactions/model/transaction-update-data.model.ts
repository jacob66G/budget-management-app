import { CategorySummary } from "./category-summary.model";

export interface TransactionUpdateData {
    title: string,
    amount: number,
    description: string,
    categories: CategorySummary[],
    categoryId: number,
    isRecurring: boolean,
    attachmentData: {originalFileName: string, downloadUrl: string} | null
}