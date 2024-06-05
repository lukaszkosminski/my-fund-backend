import { ILoadableState } from './App.model';

export interface Budget {
  id?: string;
  name: string;
  totalExpense: number;
  totalIncome: number;
  balance: number;
  expenses: Expense[];
  incomes: Income[];
}

export interface Expense {
  id: number;
  amount: number;
  idCategory: number;
  isSubCategory: number;
  name?: string;
}

export interface Income {
  id: number;
  amount: number;
  idCategory: number;
  isSubCategory: number;
  name?: string;
}

export interface BudgetState {
  budgets: Budget[];
  currentBudget: ILoadableState<Budget>;
}
