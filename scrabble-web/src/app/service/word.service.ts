import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Word } from '../model/word';

import { environment } from '../../environments/environment';

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
    return this.http.get<Word[]>(environment.GATEWAY_URL  + '/rest/games/' + gameId + '/words');
  }

}