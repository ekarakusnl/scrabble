export interface Word {
  id: number;
  gameId: number;
  userId: number;
  actionId: number;
  roundNumber: number;
  word: string;
  definition: string;
  score: number;
}