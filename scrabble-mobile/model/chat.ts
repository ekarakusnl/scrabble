export interface Chat {
  id: number,
  gameId: number;
  userId: number;
  username: string;
  message: string;
  createdDate: Date;
}