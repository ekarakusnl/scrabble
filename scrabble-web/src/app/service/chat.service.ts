import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Globals } from "../common/globals";

import { Chat } from "../model/chat";

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  constructor(
    private http: HttpClient
  ) { }

  ngOnInit(): void {
  }

  getChats(gameId: number, actionCounter: number): Observable<Chat[]> {
    return this.http.get<Chat[]>(Globals.GATEWAY_URL + '/rest/games/' + gameId + '/chats?actionCounter=' + actionCounter);
  }

  sendMessage(gameId: number, message: string): Observable<void> {
    return this.http.put<void>(Globals.GATEWAY_URL + '/rest/games/' + gameId + '/chats', message);
  }

}