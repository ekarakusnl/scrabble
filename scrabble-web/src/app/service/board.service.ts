import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Globals } from '../common/globals';

import { Board } from '../model/board';

@Injectable({
  providedIn: 'root'
})
export class BoardService {

  constructor(
    private http: HttpClient
  ) { }

  ngOnInit(): void {
  }

  getBoards(): Observable<Board[]> {
    return this.http.get<Board[]>(Globals.GATEWAY_URL + '/rest/boards');
  }

  getBoard(id: number): Observable<Board> {
    return this.http.get<Board>(Globals.GATEWAY_URL + '/rest/boards/' + id);
  }

}