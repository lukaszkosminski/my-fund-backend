import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { UserState } from '../models/User.model';
import { UserService } from '../services/user.service';
import { inject } from '@angular/core';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap } from 'rxjs';
import { tapResponse } from '@ngrx/operators';

export const UserStore = signalStore(
  { providedIn: 'root' },
  withState<UserState>({
    user: {
      username: '',
      email: '',
    },
    isLoading: true,
  }),
  withMethods((store, userService = inject(UserService)) => ({
    getCurrentUser: rxMethod<void>(
      pipe(
        switchMap(() =>
          userService.getCurrent().pipe(
            tapResponse({
              next: user => {
                patchState(store, { user, isLoading: false });
              },
              error: () => {
                patchState(store, {
                  user: {
                    username: '',
                    email: '',
                  },
                });
              },
            })
          )
        )
      )
    ),
  }))
);
