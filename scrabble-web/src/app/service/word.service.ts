import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Globals } from '../common/globals';

import { Word } from '../model/word';

@Injectable({
  providedIn: 'root'
})
export class WordService {
  constructor(
    private http: HttpClient
  ) { }

  ngOnInit(): void {
  }

  getWordLogs(gameId: number): Observable<Word[]> {
    return this.http.get<Word[]>(Globals.GATEWAY_URL  + '/rest/games/' + gameId + '/words');
  }

}