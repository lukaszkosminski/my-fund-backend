import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-form-field',
  template: `
    <label [for]="id" class="mb-2 block text-sm font-medium text-gray-900">{{
      label
    }}</label>
    <input
      [id]="id"
     [placeholder]="placeholder || ''" type="text"
      [value]="value"
      (change)="onInputChange($event)"
      class="block w-full rounded-lg border border-gray-300 bg-gray-50 p-2.5 text-gray-900 focus:border-cyan-600 focus:ring-cyan-600 sm:text-sm" />
  `,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormFieldComponent),
      multi: true,
    },
  ],
})
export class FormFieldComponent implements ControlValueAccessor {
  @Input() id: string;
  @Input() label: string;
  @Input() placeholder: string;

  private _value: any;

  set value(value: any) {
    this._value = value;
    this.onChange(value);
  }

  get value(): any {
    return this._value;
  }

  onChange = (event: any) => {};

  onInputChange = (event: any) => {
    this.value = event?.target?.value;
  };
  onTouched = () => {};

  writeValue(value: any) {
    this.value = value;
  }

  registerOnChange(fn: any) {
    this.onChange = fn;
  }

  registerOnTouched(fn: any) {
    this.onTouched = fn;
  }
}
