import { RecurringTransactionCreateRequest } from "../model/recurring-template-create-request.model";

export class RecurringTemplateMapper {

    public static toCreateRequest(formData: any): RecurringTransactionCreateRequest {
        return {
            amount: formData.amount,
            title: formData.title,
            type: formData.type,
            startDate: formData.startDate,
            recurringInterval: formData.recurringInterval,
            recurringValue: formData.recurringValue,
            description: formData.description,
            accountId: formData.account,
            categoryId: formData.category
        }
    }
}