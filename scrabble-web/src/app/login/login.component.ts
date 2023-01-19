import { Component } from '@angular/core';

import { User } from '../model/user';
import { AuthenticationService } from '../service/authentication.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  user: User = { username: '', password: '' };

  constructor(
    private authenticationService: AuthenticationService,
  ) { }

  login(): void {
    this.authenticationService.login(this.user);
  }

}
