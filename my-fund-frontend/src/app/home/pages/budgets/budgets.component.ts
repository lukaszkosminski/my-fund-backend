import {Component, inject, OnInit} from '@angular/core';
import {UserStore} from "../../../stores/user.store";
import {BudgetsStore} from "../../../stores/bugdets.store";

@Component({
  selector: 'app-budgets',
  templateUrl: './budgets.component.html',
})
export class BudgetsComponent implements OnInit {
  private readonly budgetStore = inject(BudgetsStore);

  // User = this.budgetStore.user;
  // isLoading = this.budgetStore.isLoading;

  ngOnInit(): void {
    this.budgetStore.getAll();
  }
}
