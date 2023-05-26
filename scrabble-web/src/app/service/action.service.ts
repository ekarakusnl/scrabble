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

  getAction(gameId: number, version: number): Observable<Action> {
    return this.http.get<Action>(environment.GATEWAY_URL + '/rest/games/' + gameId + '/actions/' + version);
  }

  getActions(gameId: number): Observable<Action[]> {
    return this.http.get<Action[]>(environment.GATEWAY_URL + '/rest/games/' + gameId + '/actions');
  }

}