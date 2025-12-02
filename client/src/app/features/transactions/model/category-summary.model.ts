import { CategoryType } from "../../../core/models/category-response-dto.model"

export interface CategorySummary {
    id: number,
    name: string,
    type: string,
    iconKey: string
}