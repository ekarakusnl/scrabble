import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Globals } from '../common/globals';

import { Player } from '../model/player';

@Injectable({
  providedIn: 'root'
})
export class PlayerService {

  constructor(
    private http: HttpClient
  ) { }

  ngOnInit(): void {
  }

  getPlayers(gameId: number, actionCounter: number): Observable<Player[]> {
    return this.http.get<Player[]>(Globals.GATEWAY_URL + '/rest/games/' + gameId + '/players?actionCounter=' + actionCounter);
  }

  getEffectivePlayer(gameId: number): Observable<Player> {
    return this.http.get<Player>(Globals.GATEWAY_URL + '/rest/games/' + gameId + '/players/effective');
  }

}