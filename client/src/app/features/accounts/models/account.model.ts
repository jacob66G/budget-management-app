export interface AccountDetails {
    id: number,
    type: string,
    name: string,
    balance: number,
    totalIncome: number,
    totalExpense:  number,
    currency: string,
    isDefault: boolean,
    description?: string,
    budgetType: string,
    budget?: number,
    alertThreshold?: number,
    iconPath?: string,
    includeInTotalBalance: boolean,
    createdAt: string,
    status: string,
    hasTransactions: boolean
}

export interface CreateAccount {
    type: string,
    name: string,
    currency: string,
    description?: string,
    initialBalance: number,
    budgetType: string,
    budget?: number,
    alertThreshold?: number,
    includeInTotalBalance: boolean,
    iconPath?: string,
}

export interface UpdateAccount {
    name?: string,
    currency?: string,
    description?: string,
    initialBalance?: number,
    budgetType?: string,
    budget?: number,
    alertThreshold?: number,
    includeInTotalBalance?: boolean,
    iconPath?: string
}

export interface SearchCriteria {
  type?: string;
  name?: string;
  status?: string[];
  currencies?: string[];
  budgetTypes?: string[];

  minBalance?: number;
  maxBalance?: number;

  minTotalIncome?: number;
  maxTotalIncome?: number;

  minTotalExpense?: number;
  maxTotalExpense?: number;

  minBudget?: number;
  maxBudget?: number;

  includedInTotalBalance?: boolean | null;

  createdAfter?: string;
  createdBefore?: string;

  sortBy?: string;
  sortDirection?: string;
}
