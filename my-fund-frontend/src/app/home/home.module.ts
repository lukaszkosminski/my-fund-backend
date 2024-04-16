import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {HomeComponent} from "./home.component";
import {RouterModule} from "@angular/router";
import {SidebarComponent} from "../components/sidebar/sidebar.component";


@NgModule({
  declarations: [
    HomeComponent,
    SidebarComponent

  ],
  imports: [
    CommonModule,
    RouterModule.forChild([
      { path: '',  pathMatch: 'full', component: HomeComponent },
    ]),
  ]
})
export class HomeModule { }
