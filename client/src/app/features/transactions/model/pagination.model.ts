export interface Pagination {
    page: number,
    limit: number,
    size: number,
    totalCount: number,
    totalPages: number,
    hasNext: boolean,
    hasPrevious: boolean
}