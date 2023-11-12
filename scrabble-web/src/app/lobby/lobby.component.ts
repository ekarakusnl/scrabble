import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { GameService } from '../service/game.service';
import { PlayerService } from '../service/player.service';
import { AuthenticationService } from '../service/authentication.service';

import { Game } from '../model/game';
import { Player } from '../model/player';

import { environment } from '../../environments/environment';

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.css']
})
export class LobbyComponent implements OnInit {

  profilePictureURL: string = environment.USER_IMAGE_URL;
  unixTime: number = Math.floor(Date.now() / 1000);

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
    this.gameService.getGames().subscribe((games: Game[]) => {
      this.games = [];
      if (games != null) {
        for (var game of games) {
          game.players = [];
          this.loadPlayers(game);
        }
      }
    });
  }

  loadPlayers(game: Game): void {
    this.playerService.getPlayers(game.id, game.version).subscribe((players: Player[]) => {
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
