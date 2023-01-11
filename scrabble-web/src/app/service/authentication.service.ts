import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { Globals } from "../common/globals";

import { User } from "../model/user";
import { UserToken } from "../model/user-token";

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  constructor(private http: HttpClient,
    private router: Router
  ) { }

  login(user: User) {
    this.http.post<UserToken>(Globals.GATEWAY_URL + '/login', user).subscribe((userToken: UserToken) => {
      if (userToken) {

        sessionStorage.setItem('username', user.username);
        sessionStorage.setItem('userId', String(userToken.id));
        sessionStorage.setItem('token', 'HTTP_TOKEN ' + userToken.token);
        sessionStorage.setItem('roles', JSON.stringify(userToken.roles));

        this.router.navigate(['games']).then(() => {
          window.location.reload();
        });
      } else {
        console.log("Authentication failed.", userToken)
      }
    });
  }

  logout() {
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('userId');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('roles');
    this.router.navigate(['login']).then(() => {
      window.location.reload();
    });
  }

  isAuthenticated(): boolean {
    return sessionStorage.getItem('token') !== null;
  }

  getUsername(): string {
    return sessionStorage.getItem('username') as string;
  }

  getUserId(): number {
    let userId = sessionStorage.getItem('userId');
    return userId === null ? null : Number(sessionStorage.getItem('userId'));
  }

  getRoles(): string[] {
    let roleString = sessionStorage.getItem('roles');
    return roleString ? JSON.parse(roleString) : [];
  }

  getToken(): string {
    return sessionStorage.getItem('token') as string;
  }

}