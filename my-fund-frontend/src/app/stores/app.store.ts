import {patchState, signalStore, withMethods, withState} from "@ngrx/signals";
import {inject} from "@angular/core";
import {rxMethod} from "@ngrx/signals/rxjs-interop";
import {pipe, switchMap} from "rxjs";
import {tapResponse} from '@ngrx/operators';
import {Router} from "@angular/router";
import {AppService} from "../services/app.service";
import {AppState} from "../models/App.model";


export const AppStore = signalStore(
  {providedIn: 'root'},
  withState<AppState>(
    {
      version: {
        version: 0,
        buildDate: ''
      }
    }
  ),
  withMethods(
    (
      store,
      appService = inject(AppService),
      router = inject(Router),

    ) => ({
        getVersion: rxMethod<void>(
          pipe(
            switchMap(() =>
              appService.getVersion().pipe(
                tapResponse({
                  next: (appVersion) => {
                    console.log(35, appVersion)
                    return patchState(store, {version: appVersion})
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
