import { formatDate } from '@angular/common';
import { AfterViewChecked, Component, ElementRef, Inject, LOCALE_ID, OnInit, ViewChild } from '@angular/core';
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
import { ToastService } from '../service/toast.service';
import { TranslateService } from '@ngx-translate/core';

import { Action } from '../model/action';
import { Cell } from '../model/cell';
import { Chat } from '../model/chat';
import { Game } from '../model/game';
import { Player } from '../model/player';
import { Tile } from '../model/tile';
import { Word } from '../model/word';
import { VirtualRack } from '../model/virtual-rack';
import { VirtualBoard } from '../model/virtual-board';

import { environment } from '../../environments/environment';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent implements OnInit, AfterViewChecked {
  @ViewChild('scrollMessages') private messageContainer: ElementRef
  @ViewChild('scrollActions') private actionContainer: ElementRef

  profilePictureURL: string = environment.USER_IMAGE_URL;
  unixTime: number = Math.floor(Date.now() / 1000);

  id: number;
  userId: number;
  playerNumber: number;
  game: Game;

  virtualBoard: VirtualBoard;
  virtualRack: VirtualRack;

  players: Player[];
  chats: Chat[];
  words: Word[];
  actions: Action[];
  actionMessages: string[];

  selectedTile: Tile;
  message: string;

  version: number = 0;
  currentRoundNumber: number;
  currentPlayerNumber: number;
  currentStatus: string;
  winnerPlayer: Player;
  remainingTileCount: number;
  chatMessageCount: number;

  // play duration
  durationTimer: any;
  remainingDurationInSeconds: number;
  remainingDuration: string;

  // scrolling
  newAction: boolean = false;
  newMessage: boolean = false;

  boardRowSize = 15;
  boardColumnSize = 15;
  rackSize = 7;

  constructor(
    @Inject(LOCALE_ID) private locale: string,
    private route: ActivatedRoute,
    private router: Router,
    private gameService: GameService,
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

  ngAfterViewChecked(): void {
    this.scrollMessages();
    this.scrollActions();
  }

  scrollMessages(): void {
    if (this.newMessage && this.messageContainer && this.messageContainer.nativeElement) {
      this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight;
      this.newMessage = false;
    }
  }

  scrollActions(): void {
    if (this.newAction && this.actionContainer && this.actionContainer.nativeElement) {
      this.actionContainer.nativeElement.scrollTop = this.actionContainer.nativeElement.scrollHeight;
      this.newAction = false;
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

      this.version = game.version - 1;
      this.getChats();
      this.getLastAction();
    });
  }

  getLastAction(): void {
    if (this.currentStatus === 'ENDED') {
      this.loadCells();
      this.getPlayers();
      this.getWords();
      return;
    }

    var version = this.version + 1;
    this.actionService.getAction(this.game.id, version).subscribe((action: Action) => {
      if (action && action.version) {
        this.version = action.version;
        this.currentRoundNumber = action.roundNumber;
        this.currentPlayerNumber = action.currentPlayerNumber;
        this.currentStatus = action.gameStatus;

        if (this.currentStatus === 'ENDED') {
          // game is ended, use the previous version to show the latest results
          this.version = this.version - 1; 
          this.stopTimer();
          this.getLastAction();
          return;
        } else if (this.currentStatus === 'LAST_ROUND') {
          this.toastService.info(this.translateService.instant('game.last.round'));
        }
        this.remainingTileCount = action.remainingTileCount;

        this.resetTimer(action.lastUpdatedDate);
        this.loadCells();
        this.getPlayers();
        this.getWords();
      }
      this.getLastAction();
    });
  }

  resetTimer(actionTimestamp: Date): void {
    this.stopTimer();

    this.remainingDurationInSeconds = this.game.duration;
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

  getPlayers(): void {
    this.playerService.getPlayers(this.game.id, this.version).subscribe((players: Player[]) => {
      this.players = players;
      this.setChatUsernames();
      this.playerNumber = this.players.find(player => player.userId == this.userId).playerNumber;
      if (this.currentStatus === 'WAITING') {
          // add missing players for remaining slots
          while (this.players.length < this.game.expectedPlayerCount) {
            const player: Player = { userId: 0, username: '?', playerNumber: -1, score: 0 };
            this.players.push(player);
          }
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
        this.toastService.playSound();
        this.loadRack();
      }
      this.getActions();
    });
  }

  getActions(): void {
    this.actionService.getActions(this.game.id).subscribe((actions: Action[]) => {
      this.actions = actions;
      this.actionMessages = [];
      this.actions.forEach((action: Action) => {
        this.actionMessages.push(this.getActionMessage(action));
      });
      this.newAction = true;
    });
  }

  getActionMessage(action: Action): string {
    const actionDate = '(' + formatDate(action.lastUpdatedDate,'HH:mm:ss', this.locale) +  ') ';
    if (action.type === 'CREATE') {
      return actionDate + this.translateService.instant('game.actions.create', { '0': this.getUsername(action.userId) });
    } else if (action.type === 'JOIN') {
      return actionDate + this.translateService.instant('game.actions.join', { '0': this.getUsername(action.userId) });
    } else if (action.type === 'LEAVE') {
      return actionDate + this.translateService.instant('game.actions.leave', { '0': this.getUsername(action.userId) });
    } else if (action.type === 'START') {
      return actionDate + this.translateService.instant('game.actions.start');
    } else if (action.type === 'PLAY') {
      return actionDate + this.getPlayedWordsMessage(action.userId, action.id);
    } else if (action.type === 'SKIP') {
      return actionDate + this.translateService.instant('game.actions.skip', { '0': this.getUsername(action.userId) });
    } else if (action.type === 'TIMEOUT') {
      return actionDate + this.translateService.instant('game.actions.timeout', { '0': this.getUsername(action.userId) });
    } else if (action.type === 'END') {
      return actionDate + this.translateService.instant('game.actions.end');
    } else {
      return '';
    }
  }

  setActionUsernames(): void {
    this.chats.forEach((chat: Chat) => {
      chat.username = this.getUsername(chat.userId);
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

    const boardVersion = this.version - this.game.expectedPlayerCount;
    this.virtualBoardService.getBoard(this.game.id, boardVersion).subscribe((virtualBoard: VirtualBoard) => {
      this.virtualBoard = virtualBoard;
    });
  };

  getChats(): void {
    this.chatService.getChats(this.game.id, this.chats ? this.chats.length : 0).subscribe((chats: Chat[]) => {
      if (chats && chats.length > 0) {
        this.chats = chats;
        this.setChatUsernames();
        if (this.chatMessageCount && this.chatMessageCount < chats.length) {
          this.toastService.playSound();
        }
        this.chatMessageCount = chats.length;
        this.newMessage = true;
      }
      this.getChats();
    });
  };

  setChatUsernames(): void {
    if (this.chats) {
      this.chats.forEach((chat: Chat) => {
        chat.username = this.getUsername(chat.userId);
      });
    }
  }

  getWords(): void {
    this.wordService.getWordLogs(this.game.id).subscribe((words: Word[]) => {
      if (words && words.length > 0) {
        this.words = words;
      }
    });
  };

  getCell(rowIndex: number, columnIndex: number): Cell {
    return this.virtualBoard.cells[rowIndex * this.boardColumnSize + columnIndex];
  }

  chatKeyPress(event: KeyboardEvent): void {
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
        this.selectedTile.selected = false;
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

    this.virtualRackService.exchangeTile(this.game.id, this.selectedTile.number).subscribe((tile: Tile) => {
      this.virtualRack.tiles[this.selectedTile.number - 1] = tile;
      this.selectedTile = null;
    });
  }

  getUsername(userId: number): string {
    if (!this.players) {
      return null;
    }
    const player = this.players.find(player => player.userId === userId);
    return player.username;
  }

  leaveGame(id: number): void {
    this.gameService.leaveGame(id).subscribe(() => {
      this.router.navigate(['lobby']);
    });
  }

  getPlayedWordsMessage(userId: number, actionId: number): string {
    if (!this.words) {
      return null;
    }
    const playedWords = this.words.filter((word: Word) => word.actionId === actionId)
      .map((word: Word) => word.word + '(' + word.score + ')');
    const actionMessage = playedWords.length === 1 ? 'game.actions.play.word' : 'game.actions.play.words';
    return this.translateService.instant(actionMessage, { '0': this.getUsername(userId), '1': playedWords.join(', ') })
  }

}
