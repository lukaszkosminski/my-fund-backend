import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { tap } from 'rxjs';
import { Router } from '@angular/router';
import { CreateUserPayload, User } from '../models/User.model';

const jsonPayloadHttpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  register(username: string, email: string, password: string) {
    return this.http.post<User | { [K in keyof CreateUserPayload]: string }>(
      `/register`,
      {
        username,
        email,
        password,
      },
      jsonPayloadHttpOptions
    );
  }

  login(email: string, password: string) {
    const formData = new FormData();
    formData.append('username', email);
    formData.append('password', password);

    return this.http.post(`/signin`, formData).pipe(
      tap(() => {
        localStorage.setItem('login-state', 'authenticated');
      })
    );
  }

  forgotPassword(email: string) {
    return this.http.post(
      `/request-change-password`,
      { email },
      jsonPayloadHttpOptions
    );
  }

  setNewPassword(token: string, email: string, password: string) {
    return this.http.post(
      `/change-password?email=${email}&token=${token}&newPassword=${password}`,
      {}
    );
  }

  isLoggedIn() {
    return localStorage.getItem('login-state') === 'authenticated';
  }

  logout() {
    return this.http.post(`/logout`, {}).subscribe({
      next: () => {
        localStorage.removeItem('login-state');
        this.router.navigate(['/']);
      },
      error: response => {
        console.log(26, response.error);
      },
    });
  }
}
