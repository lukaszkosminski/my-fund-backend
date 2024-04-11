import {Component} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";

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


  constructor(private authService: AuthService, private router: Router) {
  }

  onSubmit() {
    this.isLoading = true;
    console.log('Form submitted', this.form);
    this.authService.login(this.form.email, this.form.password).subscribe({
        next: () => {
          this.isLoading = false;
          this.validationErrors = null;
          this.router.navigate(['/home'])
        },
        error: (response) => {
          this.isLoading = false;
          this.validationErrors = response.error;
        }
      }
    );
  }
}
