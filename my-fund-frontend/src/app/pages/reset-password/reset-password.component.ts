import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute, Route, Router } from '@angular/router';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
})
export class ResetPasswordComponent {
  form: {
    password: string;
  } = {
    password: '',
  };

  isLoading = false;
  token = '';
  validationErrors = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.route.queryParams.subscribe(params => {
      console.log(28, params['token']);
      this.token = params['token'];
    });
  }

  onSubmit() {
    this.isLoading = true;
    console.log('Form submitted', this.form);
    this.authService.setNewPassword(this.token, this.form.password).subscribe({
      next: () => {
        this.router.navigate(['/home']);
        this.validationErrors = '';
      },
      error: () => {
        this.isLoading = false;
        this.validationErrors = 'Credentials are invalid. Please try again.';
      },
    });
  }
}
