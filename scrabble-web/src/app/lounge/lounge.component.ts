import { Component, OnInit } from '@angular/core';

import { GameService } from '../service/game.service';
import { PlayerService } from '../service/player.service';
import { Game } from '../model/game';
import { Player } from '../model/player';
import { AuthenticationService } from '../service/authentication.service';
import { Router } from '@angular/router';
import { Globals } from '../common/globals';

@Component({
  selector: 'app-lounge',
  templateUrl: './lounge.component.html',
  styleUrls: ['./lounge.component.css']
})
export class LoungeComponent implements OnInit {

  imageResourceURL: string = Globals.GATEWAY_URL;

  initialized: boolean;
  userId: number;
  username: string;
  games: Game[];

  constructor(
    private gameService: GameService,
    private playerService: PlayerService,
    private authenticationService: AuthenticationService,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.userId = this.authenticationService.getUserId();
    this.username = this.authenticationService.getUsername();
    this.loadGames();
  }

  loadGames(): void {
    this.gameService.getMyGames().subscribe((games: Game[]) => {
      this.initialized = true;
      this.games = games;
      if (games != null) {
        for (var game of games) {
          game.players = [];
          this.loadGamePlayers(game);
        }
      }
    });
  }

  loadGamePlayers(game: Game): void {
    this.playerService.getPlayers(game.id, game.actionCounter).subscribe((players: Player[]) => {
      for (var player of players) {
        game.players.push(player);
      }
      if (!players.some(gamePlayer => gamePlayer.userId == this.userId)) {
        let player: Player = { userId: this.userId, username: this.username, playerNumber: 0, score: 0 };
        game.players.push(player);
      }
      while (game.players.length < game.expectedPlayerCount) {
        let player: Player = { userId: 0, username: '?', playerNumber: -1, score: 0 };
        game.players.push(player);
      }
    });
  }

  watchGame(id: number): void {
    this.router.navigate(['games', id]);
  }

  leaveGame(id: number): void {
    this.gameService.leaveGame(id).subscribe(() => {
      this.router.navigate(['lobby']);
    });
  }

}
