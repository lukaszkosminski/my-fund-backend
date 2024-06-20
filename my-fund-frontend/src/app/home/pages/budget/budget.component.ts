import { Component, inject, OnInit } from '@angular/core';
import { BudgetsStore } from '../../../stores/bugdets.store';
import { ActivatedRoute } from '@angular/router';
import { CategoriesStore } from '../../../stores/categories.store';

@Component({
  selector: 'app-budget',
  templateUrl: './budget.component.html',
  styleUrl: './budget.component.scss',
})
export class BudgetComponent implements OnInit {
  budgetStore = inject(BudgetsStore);
  categoriesStore = inject(CategoriesStore);
  private readonly route = inject(ActivatedRoute);

  currentBudget = this.budgetStore.currentBudget;
  summary = this.budgetStore.summary;

  ngOnInit(): void {
    this.budgetStore.get(this.route.snapshot.paramMap.get('id')!);
    this.budgetStore.getSummary(this.route.snapshot.paramMap.get('id')!);
    this.categoriesStore.getAll();
  }
  getCategoryById(id: number) {
    console.log(26, this.categoriesStore.categories(), id);
    return this.categoriesStore.categories().find(category => {
      console.log(27, category.id, id);
      console.log(27, category.id === id);
      return category.id === id;
    });
  }

  deleteBudget() {
    alert('not implemented yet!');
  }
}
