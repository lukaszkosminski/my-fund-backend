import {Component, inject, OnInit} from '@angular/core';
import {BudgetsStore} from "../../../stores/bugdets.store";

@Component({
  selector: 'app-budgets',
  templateUrl: './budgets.component.html',
})
export class BudgetsComponent implements OnInit {
  private readonly budgetStore = inject(BudgetsStore);

  ngOnInit(): void {
    this.budgetStore.getAll();
  }
}
