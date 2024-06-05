import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BudgetsService } from '../../../services/budgets.service';
import { BudgetsStore } from '../../../stores/bugdets.store';
import { ActivatedRoute, Router } from '@angular/router';
import { CategoriesStore } from '../../../stores/categories.store';
import { SubCategory } from '../../../models/Category.model';

@Component({
  selector: 'app-budget-transaction-form',
  templateUrl: './budget-transaction-form.component.html',
  styleUrl: './budget-transaction-form.component.scss',
})
export class BudgetTransactionFormComponent implements OnInit {
  expenseForm: FormGroup;

  formBuilder = inject(FormBuilder);

  budgetService = inject(BudgetsService);
  budgetStore = inject(BudgetsStore);
  categoryStore = inject(CategoriesStore);
  route = inject(ActivatedRoute);
  router = inject(Router);

  categories = this.categoryStore.categories;
  subCategories: SubCategory[] = [];

  readonly budgetId = this.route.snapshot.paramMap.get('id')!;
  readonly formType: 'expenses' | 'incomes' = this.route.snapshot.paramMap.get(
    'type'
  ) as 'expenses' | 'incomes';

  ngOnInit() {
    this.expenseForm = this.formBuilder.group({
      amount: ['', Validators.required],
      idCategory: [
        '',
        this.formType === 'expenses' ? Validators.required : null,
      ],
      idSubCategory: [
        '',
        this.formType === 'expenses' ? Validators.required : null,
      ],
      name: [''],
    });

    this.categoryStore.getAll();
  }

  onCategoryChange() {
    const subCategories = this.categories().find(
      category => category.id?.toString() === this.expenseForm.value.idCategory
    )?.subCategories;

    if (subCategories) {
      this.subCategories = subCategories;
    }
  }

  onSubmit() {
    if (this.expenseForm.valid) {
      (this.formType === 'expenses'
        ? this.budgetService.addExpense(this.budgetId, this.expenseForm.value)
        : this.budgetService.addIncome(this.budgetId, this.expenseForm.value)
      ).subscribe(() => {
        this.router.navigate(['/home/budgets/', this.budgetId]);
      });

      return;
    }

    Object.keys(this.expenseForm.controls).forEach(field => {
      const control = this.expenseForm.get(field);
      control!.markAsTouched({ onlySelf: true });
    });
  }
}
