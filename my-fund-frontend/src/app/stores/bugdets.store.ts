import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { inject } from '@angular/core';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap } from 'rxjs';
import { tapResponse } from '@ngrx/operators';
import { Budget, BudgetState, Summary } from '../models/Budget.model';
import { BudgetsService } from '../services/budgets.service';
import { Router } from '@angular/router';

export const BudgetsStore = signalStore(
  { providedIn: 'root' },
  withState<BudgetState>({
    budgets: [],
    currentBudget: {
      state: 'loading',
    },
    summary: {
      state: 'loading',
    },
  }),
  withMethods(
    (
      store,
      budgetsService = inject(BudgetsService),
      router = inject(Router)
    ) => ({
      get: rxMethod<string>(
        pipe(
          tap(() => patchState(store, { currentBudget: { state: 'loading' } })),
          switchMap((id: string) =>
            budgetsService.get(id).pipe(
              tapResponse({
                next: budget =>
                  patchState(store, {
                    currentBudget: { data: budget, state: 'success' },
                  }),
                error: ({ error }) => {
                  console.log('Error', error);
                },
              })
            )
          )
        )
      ),

      getAll: rxMethod<void>(
        pipe(
          switchMap(() =>
            budgetsService.getAll().pipe(
              tapResponse({
                next: budgets => patchState(store, { budgets }),
                error: ({ error }) => {
                  console.log(33, 'error', error);
                },
              })
            )
          )
        )
      ),

      create: rxMethod<Budget>(
        pipe(
          switchMap(data =>
            budgetsService.create(data).pipe(
              tapResponse({
                next: () => {
                  router.navigate(['/home/budgets']);
                },
                error: () => {
                  console.log(33, 'error');
                },
              })
            )
          )
        )
      ),

      getSummary: rxMethod<string>(
        pipe(
          switchMap(id =>
            budgetsService.getSummary(id).pipe(
              tapResponse({
                next: (data: Summary) => {
                  console.log(81, data);
                },
                error: () => {
                  console.log(33, 'error');
                },
              })
            )
          )
        )
      ),
    })
  )
);
