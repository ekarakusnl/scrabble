import axios from 'axios';

import { Word } from '../model/word';

const WordService = {
  async list(gameId: number): Promise<Word[]> {
    return axios.get('/rest/games/' + gameId + '/words');
  },
}

export default WordService;
