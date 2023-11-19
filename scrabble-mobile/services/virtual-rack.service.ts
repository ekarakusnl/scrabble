import axios from 'axios';

import { VirtualRack } from '../model/virtual-rack';

const VirtualRackService = {

  async get(gameId: number, roundNumber: number): Promise<VirtualRack> {
    return axios.get('/rest/games/' + gameId + '/racks?roundNumber=' + roundNumber);
  },
}

export default VirtualRackService;