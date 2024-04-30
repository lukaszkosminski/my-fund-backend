import { Component, inject, OnInit } from '@angular/core';
import { BudgetsStore } from '../../../stores/bugdets.store';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-budget',
  templateUrl: './budget.component.html',
  styleUrl: './budget.component.scss',
})
export class BudgetComponent implements OnInit {
  budgetStore = inject(BudgetsStore);
  private readonly route = inject(ActivatedRoute);

  currentBudget = this.budgetStore.currentBudget;

  ngOnInit(): void {
    console.log(16, this.route.snapshot.paramMap.get('id'));
    this.budgetStore.get(this.route.snapshot.paramMap.get('id')!);
  }

  deleteBudget() {
    alert('not implemented yet!');
  }
}
