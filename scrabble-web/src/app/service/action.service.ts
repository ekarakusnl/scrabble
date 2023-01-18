import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Action } from '../model/action';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ActionService {

  constructor(
    private http: HttpClient
  ) { }

  ngOnInit(): void {
  }

  getAction(gameId: number, counter: number): Observable<Action> {
    return this.http.get<Action>(environment.GATEWAY_URL + '/rest/games/' + gameId + '/action?counter=' + counter);
  }

}