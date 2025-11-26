import { TransactionModeFilter } from "../constants/transaction-mode-filter.enum";
import { TransactionSortByFilter } from "../constants/transaction-sort-by-filter.enum";
import { TransactionSortingDirection } from "../constants/transaction-sorting-direction.enum";
import { TransactionTypeFilter } from "../constants/transaction-type-filter.enum";

export interface TransactionFilterParams {
    type: TransactionTypeFilter,
    mode: TransactionModeFilter,
    accountIds: number[] | undefined,
    categoryIds: number[] | undefined,
    since?: string,
    to?: string,
    page: number,
    limit: number,
    sortedBy: TransactionSortByFilter,
    sortDirection: TransactionSortingDirection
}