export interface Action {
  gameId: number;
  userId: number;
  version: number;
  type: string;
  gameStatus: string;
  roundNumber: number;
  currentPlayerNumber: number;
  lastUpdatedDate: Date;
}