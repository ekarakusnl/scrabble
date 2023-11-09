import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { Game } from '../model/game';
import { Board } from '../model/board';
import { BoardService } from '../service/board.service';
import { GameService } from '../service/game.service';

@Component({
  selector: 'app-create-game',
  templateUrl: './create-game.component.html',
  styleUrls: ['./create-game.component.css']
})
export class CreateGameComponent implements OnInit {

  boards: Board[] = [];
  languageCodes: string[] = ['en', 'fr', 'de', 'nl', 'tr'];

  name = new FormControl(null, Validators.required);
  playerCount = new FormControl(2, Validators.required);
  language = new FormControl('en', Validators.required);
  boardId = new FormControl(1, Validators.required);
  duration = new FormControl(3, Validators.required);
  createGameForm!: FormGroup;

  constructor(
    private boardService: BoardService,
    private gameService: GameService,
    private formBuilder: FormBuilder,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.loadBoards();

    this.createGameForm = this.formBuilder.group({
      name: this.name,
      playerCount: this.playerCount,
      language: this.language,
      boardId: this.boardId,
      duration: this.duration
    });
  }

  loadBoards(): void {
    this.boardService.getBoards().subscribe((boards: Board[]) => {
      for (var board of boards) {
        this.boards.push(board);
      }
    });
  }

  onSubmit(): void {
    const game: Game = {
      name: this.createGameForm.value.name,
      expectedPlayerCount: this.createGameForm.value.playerCount,
      language: this.createGameForm.value.language,
      boardId: this.createGameForm.value.boardId,
      duration: this.createGameForm.value.duration * 60
    };
    this.gameService.createGame(game).subscribe((game: Game) => {
      this.router.navigate(['games', game.id]);
    });
  }

}
