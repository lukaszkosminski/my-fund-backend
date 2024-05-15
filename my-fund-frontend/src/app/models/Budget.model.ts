import { ILoadableState } from './App.model';
import { SubCategory } from './Category.model';

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
  summary: ILoadableState<Summary>;
}

export interface ExpenseSummary {
  categoryId: number;
  totalExpenses: number;
  subcategories: SubCategory[];
  percentageOfTotal: number;
}

export interface Summary {
  expensesSummary: ExpenseSummary[];
}
