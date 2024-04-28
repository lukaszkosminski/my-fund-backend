import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
})

export class ButtonComponent {
  @Input({required: true}) text = ''
  @Input() type = 'button'
}

