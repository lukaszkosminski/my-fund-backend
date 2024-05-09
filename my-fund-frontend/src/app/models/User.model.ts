export interface User {
  email: string;
  username: string;
}

export interface CreateUserPayload {
  email: string;
  username: string;
  password: string;
}

export interface UserState {
  user: User;
  isLoading: boolean;
}
