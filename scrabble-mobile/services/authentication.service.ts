import axios from 'axios';
import { router } from 'expo-router';

import StorageService from './storage.service';

import { UserToken } from '../model/user-token';

const AuthenticationService = {

  async login(username: string, password: string): Promise<UserToken> {
    return axios.post('/login',
      JSON.stringify({
        username: username,
        password: password,
      }),
      { headers: { 'Content-Type': 'application/json', } }
    );
  },
  logout() {
    StorageService.evictUser();
    router.replace("/login");
  },
}

export default AuthenticationService;