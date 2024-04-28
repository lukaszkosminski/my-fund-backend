import {Component, inject, OnInit, signal, WritableSignal} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {BudgetsService} from "../../../services/budgets.service";
import {BudgetsStore} from "../../../stores/bugdets.store";
import {ActivatedRoute} from "@angular/router";
import {CategoriesStore} from "../../../stores/categories.store";
import {SubCategory} from "../../../models/Category.model";

@Component({
  selector: 'app-expense-form',
  templateUrl: './expense-form.component.html',
  styleUrl: './expense-form.component.scss'
})
export class ExpenseFormComponent implements OnInit {
  expenseForm: FormGroup;

  formBuilder = inject(FormBuilder);

  budgetService = inject(BudgetsService);
  budgetStore = inject(BudgetsStore);
  categoryStore = inject(CategoriesStore);
  route = inject(ActivatedRoute);

  categories = this.categoryStore.categories;
  subCategories: SubCategory[] = []

  ngOnInit() {
    this.expenseForm = this.formBuilder.group({
      name: ['', Validators.required],
      amount: [''],
      idCategory: [''],
      idSubCategory: ['']
    })

    this.categoryStore.getAll();
  }

  onCategoryChange() {
    console.log(this.expenseForm.value.idCategory);
    console.log(40, this.categories())
    this.subCategories = (this.categories().find((category: any) => category.id.toString() === this.expenseForm.value.idCategory)?.subCategories!);
  }

  onSubmit() {
    console.log(this.expenseForm.value);
    console.log(30, this.budgetStore.currentBudget()?.data?.id)
    const budgetId = this.route.snapshot.paramMap.get('id')!

    this.budgetService.addExpense(budgetId, this.expenseForm.value).subscribe(
      (response) => {
        console.log(response);

      }
    );
  }

}
