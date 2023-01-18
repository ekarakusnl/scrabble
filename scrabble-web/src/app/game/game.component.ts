import { AfterViewChecked, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { timer } from 'rxjs';

import { WordService } from '../service/word.service';
import { ActionService } from '../service/action.service';
import { AuthenticationService } from '../service/authentication.service';
import { VirtualBoardService } from '../service/virtual-board.service';
import { ChatService } from '../service/chat.service';
import { GameService } from '../service/game.service';
import { PlayerService } from '../service/player.service';
import { VirtualRackService } from '../service/virtual-rack.service';
import { BoardService } from '../service/board.service';
import { ToastService } from '../service/toast.service';
import { TranslateService } from '@ngx-translate/core';
import { BagService } from '../service/bag.service';

import { Action } from '../model/action';
import { Cell } from '../model/cell';
import { Chat } from '../model/chat';
import { Game } from '../model/game';
import { Player } from '../model/player';
import { Tile } from '../model/tile';
import { Board } from '../model/board';
import { Word } from '../model/word';
import { Bag } from '../model/bag';
import { VirtualRack } from '../model/virtual-rack';
import { VirtualBoard } from '../model/virtual-board';

import { environment } from '../../environments/environment';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent implements OnInit, AfterViewChecked {
  @ViewChild('scrollToBottom') private scrollContainer: ElementRef

  imageResourceURL: string = environment.USER_IMAGE_URL;

  id: number;
  userId: number;
  playerNumber: number;
  game: Game;
  board: Board;
  bag: Bag;

  virtualBoard: VirtualBoard;
  virtualRack: VirtualRack;

  players: Player[];
  chats: Chat[];
  words: Word[];

  selectedTile: Tile;
  message: string;

  actionCounter: number = 0;
  currentRoundNumber: number;
  currentPlayerNumber: number;
  currentStatus: string;
  winnerPlayer: Player;

  // play duration
  durationTimer: any;
  remainingDurationInSeconds: number;
  remainingDuration: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private gameService: GameService,
    private boardService: BoardService,
    private bagService: BagService,
    private authenticationService: AuthenticationService,
    private virtualBoardService: VirtualBoardService,
    private playerService: PlayerService,
    private chatService: ChatService,
    private virtualRackService: VirtualRackService,
    private actionService: ActionService,
    private wordService: WordService,
    private toastService: ToastService,
    private translateService: TranslateService,
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe((params: Params) => {
      this.id = params.id;
    });
    this.userId = this.authenticationService.getUserId();
    this.loadGame();
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    if (!this.scrollContainer || !this.scrollContainer.nativeElement) {
      return;
    }
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch(error) {
      this.toastService.error('An error occured');
    }
  }

  loadGame(): void {
    this.gameService.getGame(this.id).subscribe((game: Game) => {
      this.game = game;
      this.currentStatus = game.status;
      if (!game || game.status === 'TERMINATED') {
        this.router.navigate(['lobby']);
        return;
      }

      this.actionCounter = game.actionCounter - 1;
      this.loadBoard();
      this.loadBag();
      this.loadChats();
      this.loadAction();
    });
  }

  loadBoard(): void {
    this.boardService.getBoard(this.game.boardId).subscribe((board: Board) => {
      this.board = board;
    });
  }

  loadBag(): void {
    this.bagService.getBag(this.game.bagId).subscribe((bag: Bag) => {
      this.bag = bag;
    });
  }

  loadAction(): void {
    if (this.currentStatus === 'ENDED') {
      this.loadCells();
      this.loadPlayers();
      this.loadWords();
      return;
    }

    var actionCounter = this.actionCounter + 1;
    this.actionService.getAction(this.game.id, actionCounter).subscribe((action: Action) => {
      if (action && action.counter) {
        this.actionCounter = action.counter;
        this.currentRoundNumber = action.roundNumber;
        this.currentPlayerNumber = action.currentPlayerNumber;
        this.currentStatus = action.gameStatus;

        if (this.currentStatus === 'ENDED') {
          // game is ended, use the previous actionCounter to show the latest results
          this.actionCounter = this.actionCounter - 1; 
          this.stopTimer();
          this.loadAction();
          return;
        } else if (this.currentStatus === 'LAST_ROUND') {
          this.toastService.info(this.translateService.instant('game.last.round'));
        }

        this.resetTimer(action.lastUpdatedDate);
        this.loadCells();
        this.loadPlayers();
        this.loadWords();
      }
      this.loadAction();
    });
  }

  resetTimer(actionTimestamp: Date): void {
    this.stopTimer();

    this.remainingDurationInSeconds = this.game.duration * 60;
    const passedDurationInSeconds = (new Date().getTime() - new Date(actionTimestamp).getTime()) / 1000;
    const defaultDurationInSeconds = this.remainingDurationInSeconds - passedDurationInSeconds;
    this.durationTimer = timer(0, 1000).subscribe(interval => {
      this.remainingDurationInSeconds = Math.trunc(defaultDurationInSeconds - interval);
      if (this.remainingDurationInSeconds < 0) {
          this.remainingDurationInSeconds = 0;
          this.remainingDuration = '00:00';
          return;
      }

      const remainingSeconds = this.remainingDurationInSeconds % 60;
      const remainingMinutes = (this.remainingDurationInSeconds - remainingSeconds) / 60;

      const remainingSecondsString = (remainingSeconds < 10 ? '0' : '') + remainingSeconds.toString();
      const remainingMinutesString = (remainingMinutes < 10 ? '0' : '') + remainingMinutes.toString();

      this.remainingDuration = remainingMinutesString + ':' + remainingSecondsString;
    });
  }

  stopTimer(): void {
    if (this.durationTimer) {
      this.durationTimer.unsubscribe();
    }
  }

  loadPlayers(): void {
    this.playerService.getPlayers(this.game.id, this.actionCounter).subscribe((players: Player[]) => {
      this.players = players;
      this.playerNumber = this.players.find(player => player.userId == this.userId).playerNumber;
      if (this.currentStatus === 'WAITING') {
          // add missing players for remaining slots
          while (this.players.length < this.game.expectedPlayerCount) {
            const player: Player = { userId: 0, username: '?', playerNumber: -1, score: 0 };
            this.players.push(player);
          }
          return;
      } else if (this.currentStatus === 'ENDED') {
        this.winnerPlayer = players.reduce(
          (previous, current) => {
            return previous.score > current.score ? previous : current
          }
        );
        if (this.winnerPlayer.userId === this.userId) {
          this.toastService.success(this.translateService.instant('game.you.won'));
        } else {
          this.toastService.info(this.translateService.instant('game.another.player.won', { 0 : this.winnerPlayer.username }));
        }
      } else if (this.currentStatus === 'IN_PROGRESS' || this.currentStatus === 'LAST_ROUND') {
        const currentPlayer = this.players.find(player => player.playerNumber === this.currentPlayerNumber);
        if (currentPlayer.userId == this.userId) {
          this.toastService.info(this.translateService.instant('game.your.turn'));
        } else {
          this.toastService.info(this.translateService.instant('game.another.player.turn', { 0 : currentPlayer.username }));
        }
      }
      this.loadRack();
    });
  }

  loadRack(): void {
    if (this.currentStatus !== 'IN_PROGRESS' && this.currentStatus !== 'LAST_ROUND') {
        return;
    }

    let playerRoundNumber = this.currentPlayerNumber >= this.playerNumber ? this.currentRoundNumber :
      this.currentRoundNumber > 1 ? this.currentRoundNumber - 1 : 1;
    this.virtualRackService.getRack(this.game.id, playerRoundNumber).subscribe((virtualRack: VirtualRack) => {
      this.virtualRack = virtualRack;
    });
  }

  loadCells(): void {
    if (this.currentStatus === 'WAITING') {
        return;
    }

    const counter = this.actionCounter - this.game.expectedPlayerCount;
    this.virtualBoardService.getBoard(this.game.id, counter).subscribe((virtualBoard: VirtualBoard) => {
      this.virtualBoard = virtualBoard;
    });
  };

  loadChats(): void {
    this.chatService.getChats(this.game.id, this.chats ? this.chats.length + 1 : 1).subscribe((chats: Chat[]) => {
      if (chats && chats.length > 0) {
        if (!this.chats) {
          this.chats = chats;
        } else {
          this.chats = this.chats.concat(chats);
          this.toastService.playSound();
        }
      }
      this.loadChats();
    });
  };

  loadWords(): void {
    this.wordService.getWordLogs(this.game.id).subscribe((words: Word[]) => {
      if (words && words.length > 0) {
        this.words = words;
      }
    });
  };

  getCell(rowIndex: number, columnIndex: number): Cell {
    return this.virtualBoard.cells[rowIndex * this.board.columnSize + columnIndex];
  }

  chatKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.sendMessage();
    }
  };

  sendMessage(): void {
    this.chatService.sendMessage(this.game.id, this.message).subscribe(() => {
      this.message = null;
    });
  };

  selectTile(tile: Tile): void {
    if (this.playerNumber != this.currentPlayerNumber) {
      this.toastService.error(this.translateService.instant('error.2007'));
      return;
    } else if (this.remainingDurationInSeconds <= 0) {
      this.toastService.error(this.translateService.instant('error.2007'));
      return;
    }

    if (tile.sealed) {
      this.selectedTile = null;
    } else if (this.selectedTile && this.selectedTile.number === tile.number) {
      this.selectedTile.selected = false;
      this.selectedTile = null;
    } else if (this.selectedTile && this.selectedTile.number !== tile.number) {
      this.selectedTile.selected = false;
      tile.selected = true;
      this.selectedTile = tile;
    } else {
      tile.selected = true;
      this.selectedTile = tile;
    }
  }

  putTile(cell: Cell): void {
    if (this.selectedTile) {
      // put the tile to the board
      if (!cell.letter) {
        cell.letter = this.selectedTile.letter;
        cell.tileNumber = this.selectedTile.number;
        cell.value = this.selectedTile.value;
        this.selectedTile.cellNumber = cell.cellNumber;
        this.selectedTile.rowNumber = cell.rowNumber;
        this.selectedTile.columnNumber = cell.columnNumber;
        this.selectedTile.sealed = true;
        this.selectedTile = null;
      } else if (cell.letter) {
        this.toastService.error(this.translateService.instant('error.2010', { 0: cell.rowNumber, 1: cell.columnNumber }));
        this.selectedTile.sealed = false;
        this.selectedTile = null;
      }
    } else if (cell.letter && cell.tileNumber) {
      // remove the tile from the board
      const tile = this.virtualRack.tiles.find(tile => tile.number === cell.tileNumber);
      tile.sealed = false;
      tile.selected = false;
      tile.cellNumber = null;
      tile.rowNumber = null;
      tile.columnNumber = null;
      // reset the cell
      cell.letter = null;
      cell.tileNumber = null;
      cell.value = null;
    }
  }

  play(): void {
    if (this.playerNumber !== this.currentPlayerNumber) {
      this.toastService.error(this.translateService.instant('error.2007'));
      return;
    } else if (this.remainingDurationInSeconds <= 0) {
      this.toastService.error(this.translateService.instant('error.2007'));
      return;
    }
    
    this.gameService.play(this.game.id, this.virtualRack).subscribe();
  };

  exchange(): void {
    if (this.playerNumber !== this.currentPlayerNumber) {
      this.toastService.error(this.translateService.instant('error.2007'));
      return;
    } else if (this.remainingDurationInSeconds <= 0) {
      this.toastService.error(this.translateService.instant('error.2007'));
      return;
    } else if (!this.selectedTile) {
      this.toastService.warning(this.translateService.instant('game.select.tile'));
      return;
    } else if (this.virtualRack.exchanged) {
      this.toastService.warning(this.translateService.instant('error.2014'));
      return;
    }

    this.virtualRackService.exchangeTile(this.game.id, this.selectedTile.number).subscribe((virtualRack: VirtualRack) => {
      this.virtualRack = virtualRack;
      this.selectedTile = null;
    });
  }

  getUsername(userId: number): string {
    const player = this.players.find(player => player.userId === userId);
    return player.username;
  }

  leaveGame(id: number): void {
    this.gameService.leaveGame(id).subscribe(() => {
      this.router.navigate(['lobby']);
    });
  }

}
