import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { VirtualRack } from '../model/virtual-rack';

import { environment } from '../../environments/environment';

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
    return this.http.get<VirtualRack>(environment.GATEWAY_URL + '/rest/games/' + gameId + '/racks?roundNumber=' +
        roundNumber);
  }

  exchangeTile(gameId: number, tileNumber: number): Observable<VirtualRack> {
    return this.http.post<VirtualRack>(environment.GATEWAY_URL + '/rest/games/' + gameId + '/racks/tiles/' +
        tileNumber, null);
  }

}