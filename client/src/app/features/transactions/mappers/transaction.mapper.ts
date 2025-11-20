import { TransactionCreateRequest } from "../model/transaction-create-request.model";

export class TransactionMapper {
    static toCreateRequest(formData: any): TransactionCreateRequest {
        return {
            amount: formData.amount,
            title: formData.title,
            type: formData.type,
            description: formData.description,
            accountId: formData.account,
            categoryId: formData.category
          }
    }
}