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

  formState: 'idle' | 'loading' | 'success' | 'error' = 'idle';

  token = '';
  email = '';

  validationErrors = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      this.email = params['email'];
    });
  }

  onSubmit() {
    this.formState = 'loading';
    console.log('Form submitted', this.form);
    this.authService
      .setNewPassword(this.token, this.email, this.form.password)
      .subscribe({
        next: () => {
          this.formState = 'success';
          // this.router.navigate(['/home']);
          this.validationErrors = '';
        },
        error: e => {
          console.log(45, e);
          this.formState = 'error';
          this.validationErrors =
            e.error.message ?? 'Credentials are invalid. Please try again.';
        },
      });
  }
}
