import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BudgetTransactionFormComponent } from './budget-transaction-form.component';

describe('ExpenseFormComponent', () => {
  let component: BudgetTransactionFormComponent;
  let fixture: ComponentFixture<BudgetTransactionFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BudgetTransactionFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BudgetTransactionFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
