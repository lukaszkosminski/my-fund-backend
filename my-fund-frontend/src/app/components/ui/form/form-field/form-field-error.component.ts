import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-form-field-error',
  template: `
      <ng-container *ngIf="show">
          <div class="flex items-center font-normal text-red-500 text-xs mt-1 ml-1">
              {{message}}
          </div>
      </ng-container>
  `
})
export class FormFieldErrorComponent {
  @Input({required: true}) show = false
  @Input({required: true}) message = ''
}
