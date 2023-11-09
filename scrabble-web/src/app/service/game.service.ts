import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Game } from '../model/game';
import { VirtualRack } from '../model/virtual-rack';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class GameService {

  constructor(
    private http: HttpClient
  ) { }

  createGame(game: Game): Observable<Game> {
    return this.http.put<Game>(environment.GATEWAY_URL + '/rest/games', game);
  }

  getGame(id: number): Observable<Game> {
    return this.http.get<Game>(environment.GATEWAY_URL + '/rest/games/' + id);
  }

  getGames(): Observable<Game[]> {
    return this.http.get<Game[]>(environment.GATEWAY_URL + '/rest/games');
  }

  getMyGames(): Observable<Game[]> {
    return this.http.get<Game[]>(environment.GATEWAY_URL + '/rest/games/by/user');
  }

  joinGame(id: number): Observable<void> {
    return this.http.post<void>(environment.GATEWAY_URL + '/rest/games/' + id + '/join', null);
  }

  leaveGame(id: number): Observable<void> {
    return this.http.post<void>(environment.GATEWAY_URL + '/rest/games/' + id + '/leave', null);
  }

  play(id: number, virtualRack: VirtualRack): Observable<void> {
    return this.http.post<void>(environment.GATEWAY_URL + '/rest/games/' + id + '/play', virtualRack);
  }

}