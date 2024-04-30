export interface Budget {
  id?: string;
  name: string;
}

export interface BudgetState {
  budgets: Budget[];
  currentBudget: {
    data?: Budget;
    state: 'loading' | 'success' | 'error';
  };
}
