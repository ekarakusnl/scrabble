import axios from 'axios';

import { VirtualBoard } from '../model/virtual-board';

const VirtualBoardService = {

  async get(gameId: number, version: number): Promise<VirtualBoard> {
    return axios.get('/rest/games/' + gameId + '/boards?version=' + version);
  }
}

export default VirtualBoardService;