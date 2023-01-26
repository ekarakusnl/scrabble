import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { VirtualBoard } from '../model/virtual-board';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VirtualBoardService {

  constructor(
    private http: HttpClient
  ) { }

  getBoard(gameId: number, version: number): Observable<VirtualBoard> {
    return this.http.get<VirtualBoard>(environment.GATEWAY_URL + '/rest/games/' + gameId + '/boards?version=' + version);
  }

}