import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HomeComponent} from "./home.component";
import {RouterModule} from "@angular/router";
import {SidebarComponent} from "../components/sidebar/sidebar.component";
import {BudgetsComponent} from './pages/budgets/budgets.component';
import {BudgetFormComponent} from './pages/budget-form/budget-form.component';
import {ReactiveFormsModule} from "@angular/forms";


@NgModule({
  declarations: [
    HomeComponent,
    SidebarComponent,
    BudgetsComponent,
    BudgetFormComponent
  ],
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule.forChild([
      {
        path: '', component: HomeComponent, children: [
          {path: 'budgets', pathMatch: 'full', component: BudgetsComponent},
          {path: 'budgets/create', pathMatch: 'full', component: BudgetFormComponent},
        ]
      },
    ]),
  ]
})
export class HomeModule {
}
