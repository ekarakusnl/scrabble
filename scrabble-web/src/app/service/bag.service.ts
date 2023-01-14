import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Globals } from '../common/globals';

import { Bag } from '../model/bag';

@Injectable({
  providedIn: 'root'
})
export class BagService {

  constructor(
    private http: HttpClient
  ) { }

  ngOnInit(): void {
  }

  getBags(): Observable<Bag[]> {
    return this.http.get<Bag[]>(Globals.GATEWAY_URL + '/rest/bags');
  }

  getBag(id: number): Observable<Bag> {
    return this.http.get<Bag>(Globals.GATEWAY_URL + '/rest/bags/' + id);
  }

}