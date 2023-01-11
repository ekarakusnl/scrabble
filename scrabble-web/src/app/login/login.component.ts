import { Component, OnInit } from '@angular/core';

import { User } from '../model/user';
import { AuthenticationService } from '../service/authentication.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  user: User = { username: '', password: '' };

  constructor(
    private authenticationService: AuthenticationService,
  ) { }

  ngOnInit(): void {
  }

  login(): void {
    this.authenticationService.login(this.user);
  }

}
