import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Globals } from "../common/globals";
import { VirtualBoard } from "../model/virtual-board";

@Injectable({
  providedIn: 'root'
})
export class VirtualBoardService {

  constructor(
    private http: HttpClient
  ) { }

  ngOnInit(): void {
  }

  getBoard(gameId: number, actionCounter: number): Observable<VirtualBoard> {
    return this.http.get<VirtualBoard>(Globals.GATEWAY_URL + '/rest/games/' + gameId + '/boards?actionCounter=' +
        actionCounter);
  }

}