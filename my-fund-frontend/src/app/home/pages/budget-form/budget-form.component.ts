import {Component, inject, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {BudgetsService} from "../../../services/budgets.service";
import {BudgetsStore} from "../../../stores/bugdets.store";

@Component({
  selector: 'app-budget-form',
  templateUrl: './budget-form.component.html',
})
export class BudgetFormComponent implements OnInit {
  budgetForm: FormGroup;
  formBuilder = inject(FormBuilder);
  budgetStore = inject(BudgetsStore);

  ngOnInit() {
    this.budgetForm = this.formBuilder.group({
      name: ['', Validators.required],
    });
  }

  onSubmit(form: FormGroup) {
    console.log(22, form.value)
    if(form.valid) {
      this.budgetStore.create(form.value);
    }


    Object.keys(this.budgetForm.controls).forEach(field => {
      const control = this.budgetForm.get(field);
      control!.markAsTouched({ onlySelf: true });
    });
  }

  get name() {
    return this.budgetForm.get('name');
  }
}


