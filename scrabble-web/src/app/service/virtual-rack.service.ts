import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Globals } from '../common/globals';

import { VirtualRack } from '../model/virtual-rack';

@Injectable({
  providedIn: 'root'
})
export class VirtualRackService {

  constructor(
    private http: HttpClient
  ) { }

  ngOnInit(): void {
  }

  getRack(gameId: number, roundNumber: number): Observable<VirtualRack> {
    return this.http.get<VirtualRack>(Globals.GATEWAY_URL + '/rest/games/' + gameId + '/racks?roundNumber=' +
        roundNumber);
  }

  exchangeTile(gameId: number, tileNumber: number): Observable<VirtualRack> {
    return this.http.post<VirtualRack>(Globals.GATEWAY_URL + '/rest/games/' + gameId + '/racks/tiles/' +
        tileNumber, null);
  }

}