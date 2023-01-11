import { Tile } from "./tile";

export interface VirtualRack {
  exchanged: boolean;
  tiles: Tile[];
}