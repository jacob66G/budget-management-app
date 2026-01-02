import { RecurringTransactionCreateRequest } from "../model/recurring-template-create-request.model";

export class RecurringTemplateMapper {

    public static toCreateRequest(formData: any): RecurringTransactionCreateRequest {

        const date: string = formData.startDate.toLocaleDateString('sv-SE');

        return {
            amount: formData.amount,
            title: formData.title,
            type: formData.type,
            startDate: date,
            recurringInterval: formData.recurringInterval,
            recurringValue: formData.recurringValue,
            description: formData.description,
            accountId: formData.account,
            categoryId: formData.category
        }
    }
}