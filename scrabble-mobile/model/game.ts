import { Player } from './player';

export interface Game {
  id: number;
  ownerId: number;
  language: string;
  name: string;
  expectedPlayerCount: number;
  activePlayerCount: number;
  duration: number;
  status: string;
  currentPlayerNumber: number;
  roundNumber: number;
  remainingTileCount: number;
  version: number;
  players: Player[];
  createdDate: Date;
  lastUpdatedDate: Date;
}