import axios from 'axios';

import { Game } from '../model/game';
import { VirtualRack } from '../model/virtual-rack';

const GameService = {
  async create(name: string, playerCount: number, language: string, duration: number): Promise<Game> {
    return axios.put('/rest/games',
      JSON.stringify({
        name: name,
        expectedPlayerCount: playerCount,
        language: language,
        duration: duration,
      }),
      { headers: { 'Content-Type': 'application/json', } }
    );
  },
  async get(id: number): Promise<Game> {
    return axios.get('/rest/games/' + id);
  },
  async search(): Promise<Game[]> {
    return axios.get('/rest/games');
  },
  async join(id: number) {
    return axios.post('/rest/games/' + id + '/join');
  },
  async leave(id: number) {
    return axios.post('/rest/games/' + id + '/leave');
  },
  async searchByUser(): Promise<Game[]> {
    return axios.get('/rest/games/by/user');
  },
  async play(id: number, virtualRack: VirtualRack) {
    return axios.post('/rest/games/' + id + '/play',
      JSON.stringify(virtualRack),
      { headers: { 'Content-Type': 'application/json', } }
    );
  },
}

export default GameService;
