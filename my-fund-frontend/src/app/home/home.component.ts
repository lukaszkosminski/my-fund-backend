import {ChangeDetectionStrategy, Component, effect, inject, OnInit} from '@angular/core';
import {UserStore} from "../stores/user.store";
import {Store} from "@ngrx/store";
import {getState} from "@ngrx/signals";

@Component({
  selector: 'home-component',
  templateUrl: './home.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})

export class HomeComponent implements OnInit {
  private readonly userStore = inject(UserStore);

  User = this.userStore.user;
  isLoading = this.userStore.isLoading;

  ngOnInit(): void {
    this.userStore.getCurrentUser();
  }
}
