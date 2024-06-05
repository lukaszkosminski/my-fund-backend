import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
})
export class ForgotPasswordComponent {
  form: {
    email: string;
  } = {
    email: '',
  };

  emailSent = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    console.log('Form submitted', this.form);
    this.authService.forgotPassword(this.form.email).subscribe({
      next: () => {
        // this.router.navigate(['/home']);
        this.emailSent = true;
      },
      error: () => {
        this.emailSent = false;
      },
    });
  }
}
