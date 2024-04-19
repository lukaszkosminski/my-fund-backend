import {Component, inject} from '@angular/core';
import { initFlowbite } from 'flowbite';
import {AppStore} from "./stores/app.store";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'myFund';

  appStore = inject(AppStore)

  ngOnInit(): void {
    initFlowbite();
    this.appStore.getVersion();
  }
}
