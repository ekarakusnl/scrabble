import { Cell } from "./cell";

export interface DroppableCell {

  // the cell
  cell: Cell;

  // initial x coordinate
  x: number;

  // initial y coordinate
  y: number;

  // initial width
  width: number;

  // initial height
  height: number;

}