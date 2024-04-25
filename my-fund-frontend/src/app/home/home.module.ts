import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HomeComponent} from "./home.component";
import {RouterModule} from "@angular/router";
import {SidebarComponent} from "../components/sidebar/sidebar.component";
import {BudgetsComponent} from './pages/budgets/budgets.component';
import {BudgetFormComponent} from './pages/budget-form/budget-form.component';
import {ReactiveFormsModule} from "@angular/forms";
import { CategoriesComponent } from './pages/categories/categories.component';
import { CategoryFormComponent } from './pages/category-form/category-form.component';
import {UiModule} from "../components/ui/ui.module";
import { BudgetComponent } from './pages/budget/budget.component';


@NgModule({
  declarations: [
    HomeComponent,
    SidebarComponent,
    BudgetsComponent,
    BudgetFormComponent,
    CategoriesComponent,
    CategoryFormComponent,
    BudgetComponent
  ],
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule.forChild([
      {
        path: '', component: HomeComponent, children: [
          {path: 'budgets', pathMatch: 'full', component: BudgetsComponent},
          {path: 'budgets/create', pathMatch: 'full', component: BudgetFormComponent},
          {path: 'categories', pathMatch: 'full', component: CategoriesComponent},
          {path: 'categories/create', pathMatch: 'full', component: CategoryFormComponent},
          {path: 'budgets/:id', pathMatch: 'full', component: BudgetComponent},
        ]
      },
    ]),
    UiModule,
  ]
})
export class HomeModule {
}
