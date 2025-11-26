import { Pagination } from "./pagination.model";

export interface PagedResponse<T> {
    data: T[],
    pagination: Pagination,
    links: {[key: string]: string};
}
