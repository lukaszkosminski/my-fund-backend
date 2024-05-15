import {DEFAULT_CURRENCY_CODE, LOCALE_ID, NgModule} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {AppTopBarComponent} from "./components/topbar/app.topbar.component";
import {LandingComponent} from "./pages/landing/landing.component";
import {AppFooterComponent} from "./components/footer/app.footer.component";
import {LoginPage} from "./pages/login/login.page";
import {RegisterPage} from "./pages/register/register.page";
import {httpInterceptorProviders} from "./helpers/auth.interceptor";
import {HomeModule} from "./home/home.module";
import {UiModule} from "./components/ui/ui.module";
import '@angular/common/locales/global/pl';


@NgModule({
  declarations: [
    AppComponent,
    AppTopBarComponent,
    LandingComponent,
    AppFooterComponent,
    LoginPage,
    RegisterPage,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    HomeModule,
    UiModule,
  ],
  providers: [httpInterceptorProviders, {
    provide: LOCALE_ID,
    useValue: 'pl'
  },{
    provide:  DEFAULT_CURRENCY_CODE,
    useValue: 'PLN'
  },],
  bootstrap: [AppComponent]
})
export class AppModule {}
