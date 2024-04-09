import {Component} from '@angular/core';
import {AuthService} from "../../services/auth.service";

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
        error: (response) => {
          this.isLoading = false;
          console.log(37, response.error)
          this.validationErrors = response.error;
          console.log(39, this.validationErrors)

        }
      }
    );
    console.log('Form submitted', this.form);

  }
}
