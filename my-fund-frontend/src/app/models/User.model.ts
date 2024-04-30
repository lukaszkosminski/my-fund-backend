export interface User {
  email: string;
  username: string;
}

export interface UserState {
  user: User;
  isLoading: boolean;
}
