import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Globals } from "../common/globals";

import { Game } from "../model/game";
import { VirtualRack } from "../model/virtual-rack";

@Injectable({
  providedIn: 'root'
})
export class GameService {

  constructor(
    private http: HttpClient
  ) { }

  ngOnInit(): void {
  }

  createGame(game: Game): Observable<Game> {
    return this.http.put<Game>(Globals.GATEWAY_URL + '/rest/games', game);
  }

  getGame(id: number): Observable<Game> {
    return this.http.get<Game>(Globals.GATEWAY_URL + '/rest/games/' + id);
  }

  getGames(): Observable<Game[]> {
    return this.http.get<Game[]>(Globals.GATEWAY_URL + '/rest/games');
  }

  getMyGames(): Observable<Game[]> {
    return this.http.get<Game[]>(Globals.GATEWAY_URL + '/rest/games/my');
  }

  joinGame(id: number): Observable<void> {
    return this.http.post<void>(Globals.GATEWAY_URL + '/rest/games/' + id + '/join', null);
  }

  leaveGame(id: number): Observable<void> {
    return this.http.post<void>(Globals.GATEWAY_URL + '/rest/games/' + id + '/leave', null);
  }

  play(id: number, virtualRack: VirtualRack): Observable<void> {
    return this.http.post<void>(Globals.GATEWAY_URL + '/rest/games/' + id + '/play', virtualRack);
  }

}