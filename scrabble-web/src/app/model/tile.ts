export interface Tile {
  playerNo: number;
  number: number;
  rowNumber: number;
  columnNumber: number;
  cellNumber?: number;
  letter: string;
  value: number;
  vowel: boolean;
  roundNumber: number;
  sealed: boolean;
  selected?: boolean;
}