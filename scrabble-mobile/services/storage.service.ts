import AsyncStorage from '@react-native-async-storage/async-storage';

import { UserToken } from '../model/user-token';

const StorageService = {

  storeUser(userToken: UserToken) {
    AsyncStorage.setItem('userId', String(userToken.id));
    AsyncStorage.setItem('token', 'HTTP_TOKEN ' + userToken.token);
    AsyncStorage.setItem('roles', JSON.stringify(userToken.roles));
    AsyncStorage.setItem('preferredLanguage', userToken.preferredLanguage);
  },
  evictUser() {
    AsyncStorage.removeItem('userId');
    AsyncStorage.removeItem('token');
    AsyncStorage.removeItem('roles');
    AsyncStorage.removeItem('preferredLanguage');
  },
  async getToken(): Promise<string> {
    const token: string = await AsyncStorage.getItem('token');
    if (!token) {
      return Promise.resolve(null);
    }
    return token;
  },
  async getUserId(): Promise<number> {
    const userId: string = await AsyncStorage.getItem('userId');
    if (!userId) {
      return Promise.resolve(null);
    }
    return Number(userId);
  },
  async getPreferredLanguage(): Promise<string> {
    const preferredLanguage: string = await AsyncStorage.getItem('preferredLanguage');
    if (!preferredLanguage) {
      return Promise.resolve(null);
    }
    return preferredLanguage;
  },
}

export default StorageService;