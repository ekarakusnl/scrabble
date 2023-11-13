import axios from 'axios';

import { Player } from '../model/player';

const PlayerService = {
  async list(gameId: number, version: number): Promise<Player[]> {
    return axios.get('/rest/games/' + gameId + '/players?version=' + version);
  },
  async get(gameId: number): Promise<Player> {
    return axios.get('/rest/games/' + gameId + '/players/by/user');
  }
}

export default PlayerService;
