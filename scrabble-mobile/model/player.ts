export interface Player {
  userId: number;
  username: string;
  playerNumber: number;
  score: number;
  allowedActions?: string[];
}