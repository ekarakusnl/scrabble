import axios from 'axios';

import { Action } from '../model/action';

const ActionService = {

  async get(gameId: number, version: number): Promise<Action> {
    return axios.get('/rest/games/' + gameId + '/actions/' + version);
  },
  async list(gameId: number): Promise<Action[]> {
    return axios.get('/rest/games/' + gameId + '/actions');
  },
}

export default ActionService;
