import { Tile } from "./tile";

export interface Cell {
  cellNumber: number;
  rowNumber: number;
  columnNumber: number;
  color: string;
  letterValueMultiplier: number;
  wordScoreMultiplier: number;
  hasRight: boolean;
  hasLeft: boolean;
  hasTop: boolean;
  hasBottom: boolean;
  center: boolean;
  letter: string;
  value: number;
  sealed: boolean;
  roundNumber: number;
  lastPlayed: boolean;
  selectedTile: Tile;
}