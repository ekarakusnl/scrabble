export interface Action {
  gameId: number;
  userId: number;
  counter: number;
  type: string;
  status: string;
//  currentStatus: string;
//  previousStatus: string;
  roundNumber: number;
  currentPlayerNumber: number;
}