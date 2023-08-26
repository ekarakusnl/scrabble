import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { User } from '../model/user';
import { UserToken } from '../model/user-token';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  constructor(private http: HttpClient,
    private router: Router
  ) { }

  login(user: User) {
    this.http.post<UserToken>(environment.GATEWAY_URL + '/login', user).subscribe((userToken: UserToken) => {
      sessionStorage.setItem('username', user.username);
      sessionStorage.setItem('userId', String(userToken.id));
      sessionStorage.setItem('token', 'HTTP_TOKEN ' + userToken.token);
      sessionStorage.setItem('roles', JSON.stringify(userToken.roles));
      sessionStorage.setItem('preferredLanguage', userToken.preferredLanguage);

      this.router.navigate(['lobby']).then(() => {
        window.location.reload();
      });
    });
  }

  logout() {
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('userId');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('roles');
    sessionStorage.removeItem('preferredLanguage');
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
    const userId = sessionStorage.getItem('userId');
    return userId === null ? null : Number(sessionStorage.getItem('userId'));
  }

  getRoles(): string[] {
    const roleString = sessionStorage.getItem('roles');
    return roleString ? JSON.parse(roleString) : [];
  }

  getToken(): string {
    return sessionStorage.getItem('token') as string;
  }

  getPreferredLanguage(): string {
    return sessionStorage.getItem('preferredLanguage') as string;
  }

}