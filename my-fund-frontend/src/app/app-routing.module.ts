import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LandingComponent} from "./pages/landing/landing.component";
import {LoginPage} from "./pages/login/login.page";
import {RegisterPage} from "./pages/register/register.page";
import {authGuard} from "./guards/auth.guard";

const routes: Routes = [
  {path: '', pathMatch: 'full', component: LandingComponent},
  {path: 'login', pathMatch: 'full', component: LoginPage},
  {path: 'join', pathMatch: 'full', component: RegisterPage},
  {path: 'home', canActivate: [authGuard], loadChildren: () => import('./home/home.module').then(m => m.HomeModule)},
  {path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {enableViewTransitions: true})],
  providers: [],
  exports: [RouterModule]
})

export class AppRoutingModule {
}
