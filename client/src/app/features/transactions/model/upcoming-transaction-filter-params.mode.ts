import { UpcomingTransactionsTimeRange } from "../constants/upcoming-transactions-time-range.enum";

export interface UpcomingTransactionsFilterParams {
    page: number,
    limit: number,
    range: UpcomingTransactionsTimeRange,
    accountIds: number[]
}