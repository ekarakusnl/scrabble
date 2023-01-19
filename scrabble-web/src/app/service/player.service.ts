import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Player } from '../model/player';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PlayerService {

  constructor(
    private http: HttpClient
  ) { }

  getPlayers(gameId: number, actionCounter: number): Observable<Player[]> {
    return this.http.get<Player[]>(environment.GATEWAY_URL + '/rest/games/' + gameId + '/players?actionCounter=' + actionCounter);
  }

}