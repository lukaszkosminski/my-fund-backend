import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { inject } from '@angular/core';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap } from 'rxjs';
import { tapResponse } from '@ngrx/operators';
import { Router } from '@angular/router';
import { Category, CategoryState } from '../models/Category.model';
import { CategoriesService } from '../services/categories.service';

export const CategoriesStore = signalStore(
  { providedIn: 'root' },
  withState<CategoryState>({
    categories: [],
    form: {
      status: 'idle',
      message: '',
    },
  }),
  withMethods(
    (
      store,
      categoriesService = inject(CategoriesService),
      router = inject(Router)
    ) => ({
      getAll: rxMethod<void>(
        pipe(
          switchMap(() =>
            categoriesService.getAll().pipe(
              tapResponse({
                next: categories => {
                  return patchState(store, {
                    categories,
                    form: { status: 'idle', message: '' },
                  });
                },
                error: ({ error }) => {
                  console.log('Error', error);
                },
              })
            )
          )
        )
      ),

      create: rxMethod<Category>(
        pipe(
          switchMap(data => {
            patchState(store, {
              form: {
                status: 'loading',
                message: '',
              },
            });

            return categoriesService.create(data).pipe(
              tapResponse({
                next: () => {
                  router.navigate(['/home/categories']);
                },
                error: ({ error }) => {
                  console.log(33, 'error', error);
                  if (error) {
                    patchState(store, {
                      form: {
                        status: 'error',
                        message: error.message,
                      },
                    });
                  }
                },
              })
            );
          })
        )
      ),

      delete: rxMethod<Category>(
        pipe(
          switchMap(category =>
            categoriesService.delete(category).pipe(
              tapResponse({
                next: () => {
                  patchState(store, {
                    categories: store
                      .categories()
                      .filter((item: Category) => item.id !== category.id),
                  });
                },
                error: ({ error }) => {
                  alert('An error occurred while deleting the category');
                  console.log(33, 'error', error);
                },
              })
            )
          )
        )
      ),
    })
  )
);
