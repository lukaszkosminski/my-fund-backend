export interface AppState {
  version: IAppVersion;
}

export interface IAppVersion {
  buildDate: string,
  version: number
}

export interface ILoadableState<T> {
  data?: T,
  state: 'loading' | 'success' | 'error';
}

