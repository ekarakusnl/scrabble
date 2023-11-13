import axios from 'axios';

import { Chat } from '../model/chat';

const ChatService = {
  async list(gameId: number, messageCount: number): Promise<Chat[]> {
    return axios.get('/rest/games/' + gameId + '/chats?messageCount=' + messageCount);
  },
  async save(gameId: number, message: string): Promise<void> {
    return axios.put('/rest/games/' + gameId + '/chats', message);
  }
}

export default ChatService;
