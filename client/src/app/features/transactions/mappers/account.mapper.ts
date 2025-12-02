import { Account } from "../../../core/models/account.model";
import { AccountSummary } from "../model/account-summary.model";

export class AccountMapper {
    static toSummary(dto: Account): AccountSummary {
        return {
            id: dto.id,
            name: dto.name,
            currency: dto.currency
        }
    }
}