export interface Budget {
  id?: string;
  name: string;
  totalExpense: number;
  totalIncome: number;
  balance: number;
  expenses: {
    id: number;
    amount: number;
    idCategory: number;
    isSubCategory: number;
  }[];
}

export interface BudgetState {
  budgets: Budget[];
  currentBudget: {
    data?: Budget;
    state: 'loading' | 'success' | 'error';
  }
}
