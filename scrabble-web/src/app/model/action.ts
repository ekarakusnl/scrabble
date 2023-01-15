export interface Action {
  gameId: number;
  userId: number;
  counter: number;
  type: string;
  gameStatus: string;
  roundNumber: number;
  currentPlayerNumber: number;
  lastUpdatedDate: Date;
}