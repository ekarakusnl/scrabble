import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Bag } from '../model/bag';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BagService {

  constructor(
    private http: HttpClient
  ) { }

  getBags(): Observable<Bag[]> {
    return this.http.get<Bag[]>(environment.GATEWAY_URL + '/rest/bags');
  }

  getBag(id: number): Observable<Bag> {
    return this.http.get<Bag>(environment.GATEWAY_URL + '/rest/bags/' + id);
  }

}