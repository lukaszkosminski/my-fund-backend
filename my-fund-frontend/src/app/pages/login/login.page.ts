import {Component} from '@angular/core';
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'login-page',
  templateUrl: './login.page.html'
})

export class LoginPage {
  form: {
    email: string;
    password: string;
  } = {
    email: '',
    password: ''
  }

  isLoading = false;

  validationErrors = null;

  constructor(private authService: AuthService) {
  }

  onSubmit() {
    this.isLoading = true;
    console.log('Form submitted', this.form);
    this.authService.login(this.form.email, this.form.password).subscribe({
        next: () => {
          this.isLoading = false;
          this.validationErrors = null;
        },
        error: (response) => {
          this.isLoading = false;
          console.log(37, response.error)
          this.validationErrors = response.error;
          console.log(39, this.validationErrors)
        }
      }
    );
  }
}
