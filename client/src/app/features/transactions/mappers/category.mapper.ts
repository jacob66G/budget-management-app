
import { Category } from "../../../core/models/category.model";
import { CategorySummary } from "../model/category-summary.model";

export class CategoryMapper {
    static toSummary(dto: Category): CategorySummary {
        return {
            id: dto.id,
            name: dto.name,
            type: dto.type,
            iconKey: dto.iconPath
        }
    }
}