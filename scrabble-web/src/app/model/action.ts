export interface Action {
  id: number;
  gameId: number;
  userId: number;
  version: number;
  type: string;
  gameStatus: string;
  roundNumber: number;
  currentPlayerNumber: number;
  remainingTileCount: number;
  lastUpdatedDate: Date;
}