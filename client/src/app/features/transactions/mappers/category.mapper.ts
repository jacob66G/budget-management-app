import { CategoryResponseDto } from "../../../core/models/category-response-dto.model";
import { CategorySummary } from "../model/category-summary.model";

export class CategoryMapper {
    static toSummary(dto: CategoryResponseDto): CategorySummary {
        return {
            id: dto.id,
            name: dto.name,
            type: dto.type,
            iconKey: dto.iconKey
        }
    }
}