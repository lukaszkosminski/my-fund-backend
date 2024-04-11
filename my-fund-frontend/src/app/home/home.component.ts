import {Component} from '@angular/core';
import {UserService} from "../services/user.service";
import {AuthService} from "../services/auth.service";

@Component({
  selector: 'home-component',
  templateUrl: './home.component.html'
})

export class HomeComponent {
  User = {
    username: ''
  }

  constructor(private userService: UserService, private authService: AuthService) {
    console.log(16, 'asd')
    this.userService.getCurrent().subscribe({
      next: (response) => {
        console.log(11, response)
        this.User = response
      },
      error: (response) => {
        console.log(14, response.error)
      }
    })

  }

  logout = () => {
    this.authService.logout();
  }

}
