import { Tile } from "./tile";

export interface DraggableTile {

  // the tile
  tile: Tile;

  // initial x coordinate
  x: number;

  // initial y coordinate
  y: number;

  // initial width
  width: number;

  // initial height
  height: number;

}