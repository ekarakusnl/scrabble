import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Board } from '../model/board';

import { environment } from '../../environments/environment';

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
    return this.http.get<Board[]>(environment.GATEWAY_URL + '/rest/boards');
  }

  getBoard(id: number): Observable<Board> {
    return this.http.get<Board>(environment.GATEWAY_URL + '/rest/boards/' + id);
  }

}