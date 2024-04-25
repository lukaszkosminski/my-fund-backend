import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from './button/button.component';
import { FormFieldComponent } from './form/form-field/form-field.component';
import {FormsModule} from "@angular/forms";
import {FormFieldErrorComponent} from "./form/form-field/form-field-error.component";
import { FormRowComponent } from './form/form-row/form-row.component';



@NgModule({
  declarations: [
    ButtonComponent,
    FormFieldComponent,
    FormFieldErrorComponent,
    FormRowComponent,
  ],
  exports: [
    ButtonComponent,
    FormFieldComponent,
    FormFieldErrorComponent,
    FormRowComponent
  ],
  imports: [
    CommonModule,
    FormsModule
  ]
})
export class UiModule { }
