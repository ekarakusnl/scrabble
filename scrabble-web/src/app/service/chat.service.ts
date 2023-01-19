import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Chat } from '../model/chat';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  constructor(
    private http: HttpClient
  ) { }

  getChats(gameId: number, actionCounter: number): Observable<Chat[]> {
    return this.http.get<Chat[]>(environment.GATEWAY_URL + '/rest/games/' + gameId + '/chats?actionCounter=' + actionCounter);
  }

  sendMessage(gameId: number, message: string): Observable<void> {
    return this.http.put<void>(environment.GATEWAY_URL + '/rest/games/' + gameId + '/chats', message);
  }

}