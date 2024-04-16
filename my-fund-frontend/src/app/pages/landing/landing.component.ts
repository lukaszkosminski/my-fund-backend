import { Component } from '@angular/core';
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'landing-component',
  templateUrl: './landing.component.html'
})
export class LandingComponent {
  isLoggedIn = this.authService.isLoggedIn();

  constructor(private authService: AuthService) { }
}
