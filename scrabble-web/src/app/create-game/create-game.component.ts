import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { Game } from '../model/game';
import { Bag } from '../model/bag';
import { Board } from '../model/board';
import { BagService } from '../service/bag.service';
import { BoardService } from '../service/board.service';
import { GameService } from '../service/game.service';

@Component({
  selector: 'app-create-game',
  templateUrl: './create-game.component.html',
  styleUrls: ['./create-game.component.css']
})
export class CreateGameComponent implements OnInit {

  bags: Bag[] = [];
  boards: Board[] = [];

  name = new FormControl(null, Validators.required);
  playerCount = new FormControl(null, Validators.required);
  bagId = new FormControl(2, Validators.required);
  boardId = new FormControl(1, Validators.required);
  duration = new FormControl(null, Validators.required);
  createGameForm!: FormGroup;

  constructor(
    private bagService: BagService,
    private boardService: BoardService,
    private gameService: GameService,
    private formBuilder: FormBuilder,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.loadBoards();
    this.loadBags();

    this.createGameForm = this.formBuilder.group({
      name: this.name,
      playerCount: this.playerCount,
      bagId: this.bagId,
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

  loadBags(): void {
    this.bagService.getBags().subscribe((bags: Bag[]) => {
      for (var bag of bags) {
        this.bags.push(bag);
      }
    });
  }

  onSubmit(): void {
    const game: Game = {
      name: this.createGameForm.value.name,
      expectedPlayerCount: this.createGameForm.value.playerCount,
      bagId: this.createGameForm.value.bagId,
      boardId: this.createGameForm.value.boardId,
      duration: this.createGameForm.value.duration
    };
    this.gameService.createGame(game).subscribe((game: Game) => {
      this.router.navigate(['lounge']);
    });
  }

}
