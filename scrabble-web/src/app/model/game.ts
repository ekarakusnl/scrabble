import { Player } from "./player";

export interface Game {
  id?: number;
  ownerId?: number;
  bagId?: number;
  boardId?: number;
  name?: string;
  expectedPlayerCount?: number;
  activePlayerCount?: number;
  duration?: number;
  status?: string;
  currentPlayerNumber?: number;
  roundNumber?: number;
  actionCounter?: number;
  players?: Player[];
}