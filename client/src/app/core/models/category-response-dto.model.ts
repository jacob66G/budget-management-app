export interface CategoryResponseDto {
    id: number,
    name: string,
    type: CategoryType
    isDefault: boolean,
    iconKey: string
}

export enum CategoryType {
    INCOME = 'income',
    EXPENSE = 'expense',
    GENERAL = 'general'
}