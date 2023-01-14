export interface Action {
  gameId: number;
  userId: number;
  counter: number;
  type: string;
  status: string;
  roundNumber: number;
  currentPlayerNumber: number;
  lastUpdatedDate: Date;
}