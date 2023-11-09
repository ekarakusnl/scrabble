import { Board } from './board';
import { Player } from './player';

export interface Game {
  id?: number;
  ownerId?: number;
  language?: string;
  boardId?: number;
  name?: string;
  expectedPlayerCount?: number;
  activePlayerCount?: number;
  duration?: number;
  status?: string;
  currentPlayerNumber?: number;
  roundNumber?: number;
  remainingTileCount?: number;
  version?: number;
  board?: Board;
  players?: Player[];
}