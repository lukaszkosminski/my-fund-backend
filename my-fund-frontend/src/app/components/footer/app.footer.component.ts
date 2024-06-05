import { Component, inject } from '@angular/core';
import { AppStore } from '../../stores/app.store';

@Component({
  selector: 'app-footer',
  templateUrl: './app.footer.component.html',
})
export class AppFooterComponent {
  appStore = inject(AppStore);

  appVersion = this.appStore.version;
}
