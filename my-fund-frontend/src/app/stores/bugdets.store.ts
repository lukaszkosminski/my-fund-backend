import {patchState, signalStore, withMethods, withState} from "@ngrx/signals";
import {UserState} from "../models/User.model";
import {UserService} from "../services/user.service";
import {inject} from "@angular/core";
import {rxMethod} from "@ngrx/signals/rxjs-interop";
import {pipe, switchMap, tap} from "rxjs";
import {tapResponse} from '@ngrx/operators';
import {Budget, BudgetState} from "../models/Budget.model";
import {BudgetsService} from "../services/budgets.service";
import {Router} from "@angular/router";


export const BudgetsStore = signalStore(
  {providedIn: 'root'},
  withState<BudgetState>(
    {
      budgets: []
    }
  ),
  withMethods(
    (
      store,
      budgetsService = inject(BudgetsService),
      router = inject(Router),

    ) => ({
        getAll: rxMethod<void>(
          pipe(
            switchMap((data: any) =>
              budgetsService.getAll().pipe(
                tapResponse({
                  next: () => patchState(store, {budgets: data}),
                  error: ({error}) => {
                    console.log(33, 'error')
                  },
                }),
              ),
            ),
          ),
        ),

        create: rxMethod<Budget>(
          pipe(
            switchMap((data) =>
              budgetsService.create(data).pipe(
                tapResponse({
                  next: () => {
                    router.navigate(['/home/budgets'])
                  },
                  error: () => {
                    console.log(33, 'error')
                  },
                }),
              ),
            ),
          ),
        ),
      }
    )
  )
)
