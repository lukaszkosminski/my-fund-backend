import {Component} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {CreateUserPayload, User} from "../../models/User.model";

@Component({
  selector: 'register-page',
  templateUrl: './register.page.html'
})

export class RegisterPage {
  form: {
    email: string;
    username: string;
    password: string;
  } = {
    email: '',
    username: '',
    password: ''
  }

  isLoading = false;
  userCreated = false;
  noFormErrors = {
    email: "",
    username: "",
    password: "",
  }

  validationErrors = this.noFormErrors

  constructor(private authService: AuthService) {
  }

  onSubmit() {
    this.isLoading = true;
    this.validationErrors = this.noFormErrors;

    this.authService.register(this.form.username, this.form.email, this.form.password).subscribe({
        next: () => {
          this.userCreated = true;
          this.isLoading = false;
        },
        error: (response: {error: {[K in keyof CreateUserPayload]: string}}) => {
          this.isLoading = false;
          this.validationErrors = response.error;
        }
      }
    );
  }
}
