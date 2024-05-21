import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CreateUserPayload } from '../../models/User.model';

@Component({
  selector: 'app-register-page',
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  form: {
    email: string;
    username: string;
    password: string;
  } = {
    email: '',
    username: '',
    password: '',
  };

  isLoading = false;
  userCreated = false;
  noFormErrors = {
    email: '',
    username: '',
    password: '',
  };

  validationErrors = this.noFormErrors;

  constructor(private authService: AuthService) {}

  onSubmit() {
    this.isLoading = true;
    this.validationErrors = this.noFormErrors;

    this.authService
      .register(this.form.username, this.form.email, this.form.password)
      .subscribe({
        next: () => {
          this.userCreated = true;
          this.isLoading = false;
        },
        error: (response: {
          error: { [K in keyof CreateUserPayload]: string };
        }) => {
          this.isLoading = false;
          this.validationErrors = response.error;
        },
      });
  }
}
