import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { GameService } from '../service/game.service';
import { PlayerService } from '../service/player.service';
import { AuthenticationService } from '../service/authentication.service';
import { BoardService } from '../service/board.service';
import { BagService } from '../service/bag.service';

import { Game } from '../model/game';
import { Player } from '../model/player';
import { Board } from '../model/board';
import { Bag } from '../model/bag';

import { environment } from '../../environments/environment';

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.css']
})
export class LobbyComponent implements OnInit {

  imageResourceURL: string = environment.USER_IMAGE_URL;

  userId: number;
  username: string;
  games: Game[];

  constructor(
    private gameService: GameService,
    private playerService: PlayerService,
    private boardService: BoardService,
    private bagService: BagService,
    private authenticationService: AuthenticationService,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.userId = this.authenticationService.getUserId();
    this.username = this.authenticationService.getUsername();
    this.loadGames();
  }

  loadGames(): void {
    this.gameService.getGames().subscribe((games: Game[]) => {
      this.games = [];
      if (games != null) {
        for (var game of games) {
          game.players = [];
          this.loadBoard(game);
          this.loadBag(game);
          this.loadPlayers(game);
        }
      }
    });
  }

  loadBoard(game: Game): void {
    this.boardService.getBoard(game.boardId).subscribe((board: Board) => {
      game.board = board;
    });
  }

  loadBag(game: Game): void {
    this.bagService.getBag(game.bagId).subscribe((bag: Bag) => {
      game.bag = bag;
    });
  }

  loadPlayers(game: Game): void {
    this.playerService.getPlayers(game.id, game.actionCounter).subscribe((players: Player[]) => {
      const userExist = players.some(gamePlayer => gamePlayer.userId == this.userId);
      if (userExist) {
          return;
      }
      this.games.push(game);

      // add joined players
      for (var player of players) {
        game.players.push(player);
      }
      
      // add missing players for remaining slots
      while (game.players.length < game.expectedPlayerCount) {
        const player: Player = { userId: 0, username: '?', playerNumber: -1, score: 0 };
        game.players.push(player);
      }
    });
  }

  joinGame(id: number): void {
    this.gameService.joinGame(id).subscribe(() => {
      this.router.navigate(['games', id]);
    });
  }

}
