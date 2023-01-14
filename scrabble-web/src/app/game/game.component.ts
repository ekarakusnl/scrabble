import { AfterViewChecked, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { Action } from '../model/action';
import { Cell } from '../model/cell';
import { Chat } from '../model/chat';
import { Game } from '../model/game';
import { Player } from '../model/player';
import { Tile } from '../model/tile';
import { Board } from '../model/board';
import { Word } from '../model/word';

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
import { VirtualRack } from '../model/virtual-rack';
import { VirtualBoard } from '../model/virtual-board';
import { TranslateService } from '@ngx-translate/core';
import { Globals } from '../common/globals';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent implements OnInit, AfterViewChecked {
  @ViewChild('scrollToBottom') private scrollContainer: ElementRef

  initialized: boolean = false;

  imageResourceURL: string = Globals.GATEWAY_URL;

  id: number;
  playerId: number;
  game: Game;
  board: Board;

  virtualBoard: VirtualBoard;
  virtualRack: VirtualRack;

  players: Player[];
  chats: Chat[];
  words: Word[];

  effectivePlayer: Player;
  selectedTile: Tile;
  message: string;

  actionCounter: number = 0;
  currentRoundNumber: number;
  currentPlayerNumber: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private gameService: GameService,
    private boardService: BoardService,
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
    this.playerId = this.authenticationService.getUserId();
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
      if (game != null && game.status === 'IN_PROGRESS') {
        this.actionCounter = game.actionCounter - 1;
        this.playerService.getEffectivePlayer(this.game.id).subscribe((player: Player) => {
          this.effectivePlayer = player;
          this.loadBoard();
          this.loadAction();
          this.loadChats();
        });
      } else if (game.status === 'LAST_ROUND') {
        this.actionCounter = game.actionCounter - 1;
        this.playerService.getEffectivePlayer(this.game.id).subscribe((player: Player) => {
          this.effectivePlayer = player;
          this.loadBoard();
          this.loadAction();
          this.loadChats();
        });
      } else if (game.status === 'ENDED') {
        this.actionCounter = game.actionCounter - 1;
        this.playerService.getEffectivePlayer(this.game.id).subscribe((player: Player) => {
          this.effectivePlayer = player;
          this.loadBoard();
          this.loadPlayers();
          this.loadCells();
          this.loadChats();
        });
      } else {
        this.router.navigate(['lobby']);
      }
    });
  }

  loadBoard(): void {
    this.boardService.getBoard(this.game.boardId).subscribe((board: Board) => {
      this.board = board;
      this.initialized = true;
    });
  }

  loadEffectivePlayer(): void {
    this.playerService.getEffectivePlayer(this.game.id).subscribe((player: Player) => {
      this.effectivePlayer = player;
      this.loadBoard();
      this.loadAction();
      this.loadChats();
    });
  }

  loadAction(): void {
    var actionCounter = this.actionCounter + 1;
    this.actionService.getAction(this.game.id, actionCounter).subscribe((action: Action) => {
      if (action != null && action.counter != null) {
        this.actionCounter = action.counter;
        this.currentRoundNumber = action.roundNumber;
        this.currentPlayerNumber = action.currentPlayerNumber;
        if (action.type == 'END') {
          this.loadGame();
          return;
        } else if (action.status == 'LAST_ROUND') {
          this.toastService.info(this.translateService.instant('game.last.round'));
        }
        this.loadPlayers();
        this.loadCells();
        this.loadRack();
        if (this.effectivePlayer != null && this.currentPlayerNumber == this.effectivePlayer.playerNumber) {
          this.toastService.info(this.translateService.instant('game.your.turn'));
        }
      }
      this.loadAction();
    });
  }

  loadPlayers(): void {
    this.playerService.getPlayers(this.game.id, this.actionCounter).subscribe((players: Player[]) => {
      this.players = players;
      if (this.game.status === 'ENDED') {
        const winner = players.reduce(
          (previous, current) => {
            return previous.score > current.score ? previous : current
          }
        );
        if (winner.userId === this.playerId) {
          this.toastService.success(this.translateService.instant('game.you.won'));
        } else {
          this.toastService.info(this.translateService.instant('game.another.player.won', { 0 : winner.username }));
        }
      }
    });
  }

  loadRack(): void {
    let playerRoundNumber = this.currentPlayerNumber >= this.effectivePlayer.playerNumber ? this.currentRoundNumber : this.currentRoundNumber > 1 ? this.currentRoundNumber - 1 : 1;
    this.virtualRackService.getRack(this.game.id, playerRoundNumber).subscribe((virtualRack: VirtualRack) => {
      this.virtualRack = virtualRack;
    });
  }

  loadCells(): void {
    let counter = this.actionCounter - this.game.expectedPlayerCount;
    this.virtualBoardService.getBoard(this.game.id, counter).subscribe((virtualBoard: VirtualBoard) => {
      this.virtualBoard = virtualBoard;
      this.loadWords();
    });
  };

  loadChats(): void {
    this.chatService.getChats(this.game.id, this.chats != null ? this.chats.length + 1 : 1).subscribe((chats: Chat[]) => {
      if (chats != null && chats.length > 0) {
        if (this.chats == null) {
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
      if (words != null && words.length > 0) {
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
    if (this.effectivePlayer.playerNumber != this.currentPlayerNumber) {
      this.toastService.error(this.translateService.instant('error.2007'));
      return;
    }
    if (tile.sealed) {
      this.selectedTile = null;
    } else if (this.selectedTile != null && this.selectedTile.number == tile.number) {
      this.selectedTile.selected = false;
      this.selectedTile = null;
    } else if (this.selectedTile != null && this.selectedTile.number != tile.number) {
      this.selectedTile.selected = false;
      tile.selected = true;
      this.selectedTile = tile;
    } else {
      tile.selected = true;
      this.selectedTile = tile;
    }
  }

  putTile(cell: Cell): void {
    if (this.selectedTile != null) {
      // put the tile to the board
      if (cell.letter == null) {
        cell.letter = this.selectedTile.letter;
        cell.tileNumber = this.selectedTile.number;
        cell.value = this.selectedTile.value;
        this.selectedTile.cellNumber = cell.cellNumber;
        this.selectedTile.rowNumber = cell.rowNumber;
        this.selectedTile.columnNumber = cell.columnNumber;
        this.selectedTile.sealed = true;
        this.selectedTile = null;
      } else if (cell.letter != null) {
        this.toastService.error(this.translateService.instant('error.2010', {0 : cell.rowNumber, 1: cell.columnNumber}));
        this.selectedTile.sealed = false;
        this.selectedTile = null;
      }
    } else if (cell.letter != null && cell.tileNumber != null) {
      // remove the tile from the board
      let tile = this.virtualRack.tiles.find(tile => tile.number == cell.tileNumber);
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
    if (this.effectivePlayer.playerNumber != this.currentPlayerNumber) {
      this.toastService.error(this.translateService.instant('error.2007'));
      return;
    }
    this.gameService.play(this.game.id, this.virtualRack).subscribe();
  };

  exchange(): void {
    if (this.selectedTile == null) {
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

  getUsername(playerNumber: number): string {
    let player = this.players.find(player => player.playerNumber == playerNumber);
    return player.username;
  }

}
