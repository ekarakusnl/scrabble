import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { Game } from '../model/game';
import { Player } from '../model/player';
import { Board } from '../model/board';
import { AuthenticationService } from '../service/authentication.service';
import { PlayerService } from '../service/player.service';
import { GameService } from '../service/game.service';
import { BoardService } from '../service/board.service';
import { ActionService } from '../service/action.service';
import { Action } from '../model/action';

@Component({
  selector: 'app-game-lounge',
  templateUrl: './game-lounge.component.html',
  styleUrls: ['./game-lounge.component.css']
})
export class GameLoungeComponent implements OnInit {

  id: number;
  userId: number;
  game: Game;
  board: Board;

  actionCounter: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authenticationService: AuthenticationService,
    private gameService: GameService,
    private playerService: PlayerService,
    private boardService: BoardService,
    private actionService: ActionService,
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe((params: Params) => {
      this.id = params.id;
    });
    this.userId = this.authenticationService.getUserId();
    this.loadGame();
  }

  loadGame(): void {
    this.gameService.getGame(this.id).subscribe((game: Game) => {
      this.game = game;
      if (game.status === 'READY' || game.status === 'IN_PROGRESS') {
        this.router.navigate(['games', game.id]);
      } else {
        this.actionCounter = game.actionCounter;
        this.loadBoard();
        this.loadPlayers();
        this.getAction();
      }
    });
  }

  loadBoard(): void {
    this.boardService.getBoard(this.game.boardId).subscribe((board: Board) => {
      this.board = board;
    });
  }

  loadPlayers(): void {
    this.playerService.getPlayers(this.game.id, this.actionCounter).subscribe((players: Player[]) => {
      if (players.length == this.game.expectedPlayerCount) {
        this.router.navigate(['games', this.game.id]);
      } else {
        while (players.length < this.game.expectedPlayerCount) {
          let waitingPlayer: Player = { userId: 0, username: '?', playerNumber: -1, score: 0 };
          players.push(waitingPlayer);
        }
        this.game.players = players;
      }
    });
  }

  leaveGame(id: number): void {
    this.gameService.leaveGame(id).subscribe(() => {
      this.router.navigate(['games']);
    });
  }

  getAction(): void {
    let actionCounter = this.actionCounter + 1;
    this.actionService.getAction(this.game.id, actionCounter).subscribe((action: Action) => {
      if (action != null && action.counter != null) {
        this.actionCounter = action.counter;
        this.loadPlayers();
      }
      this.getAction();
    });
  }

}
