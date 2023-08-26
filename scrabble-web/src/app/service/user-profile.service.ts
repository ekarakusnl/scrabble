import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { UserProfile } from '../model/user-profile';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserProfileService {

  constructor(
    private http: HttpClient
  ) { }

  getUser(): Observable<UserProfile> {
    return this.http.get<UserProfile>(environment.GATEWAY_URL + '/rest/users/current');
  }

  saveUser(user: UserProfile): Observable<UserProfile> {
    return this.http.put<UserProfile>(environment.GATEWAY_URL + '/rest/users', user);
  }

  saveProfilePicture(file: File): Observable<void> {
    const formData: FormData = new FormData();
    formData.append('file', file);

    return this.http.post<void>(environment.GATEWAY_URL + '/rest/images/profile', formData);
  }

}