import {Component} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'login-page',
  templateUrl: './login.page.html'
})

export class LoginPage {
  form: {
    username: string;
    password: string;
  } = {
    username: '',
    password: ''
  }

  isLoading = false;

  validationErrors = '';


  constructor(private authService: AuthService, private router: Router) {
  }

  onSubmit() {
    this.isLoading = true;
    console.log('Form submitted', this.form);
    this.authService.login(this.form.username, this.form.password).subscribe({
        next: () => {
          this.router.navigate(['/home'])
          this.validationErrors = '';
        },
        error: () => {
          this.isLoading = false;
          this.validationErrors = 'Credentials are invalid. Please try again.';
        }
      }
    );
  }
}
