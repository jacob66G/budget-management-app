import { AccountResponseDto } from "../../../core/models/account-response-dto.model";
import { AccountSummary } from "../model/account-summary.model";

export class AccountMapper {
    static toSummary(dto: AccountResponseDto): AccountSummary {
        return {
            id: dto.id,
            name: dto.name,
            currency: dto.currency
        }
    }
}