import { Component } from '@angular/core';

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

  constructor() { }

  onSubmit() {
    this.isLoading = true;
    console.log('Form submitted', this.form);

  }
}
