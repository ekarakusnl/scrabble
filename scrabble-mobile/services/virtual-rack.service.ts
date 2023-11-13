import axios from 'axios';

import { Tile } from '../model/tile';
import { VirtualRack } from '../model/virtual-rack';

const VirtualRackService = {

  async get(gameId: number, roundNumber: number): Promise<VirtualRack> {
    return axios.get('/rest/games/' + gameId + '/racks?roundNumber=' + roundNumber);
  },
  async exchangeTile(gameId: number, tileNumber: number): Promise<Tile> {
    return axios.post('/rest/games/' + gameId + '/racks/tiles/' + tileNumber,
      { headers: { 'Content-Type': 'application/json', } }
    );
  },
}

export default VirtualRackService;