import axios from 'axios';
import { TFunction } from 'i18next';

import AuthenticationService from '../services/authentication.service';
import StorageService from '../services/storage.service';

const AxiosInterceptor = {
  createRequestInterceptor() {
    return axios.interceptors.request.use(
      async (config) => {
        // add the host url if the request url does not start with it
        if (!config.url.startsWith(process.env.EXPO_PUBLIC_GATEWAY_URL)) {
          config.url = process.env.EXPO_PUBLIC_GATEWAY_URL + config.url;
        }

        // add the token header if the user is authenticated
        const token = await StorageService.getToken();
        if (token) {
          config.headers.Authorization = token;
        }

        return config;
      },
      error => {
        return Promise.reject(error);
      }
    );
  },
  createResponseInterceptor(t: TFunction) {
    return axios.interceptors.response.use(
      response => {
        if (response && response.data !== undefined) {
          // user token has been returned, store the token
          if (response.data.token !== undefined) {
            StorageService.storeUser(response.data);
          }
          return response.data;
        }
        return response;
      },
      error => {
        if (!error) {
          AuthenticationService.logout();
          return Promise.reject('An error occured, please try again later.');
        }

        if (error.response === undefined) {
          return Promise.reject(error);
        }

        const response = error.response;
        if (!response) {
          return Promise.reject(error);
        }

        if (response.status === 401) {
          AuthenticationService.logout();
          return Promise.reject();
        }

        let message: string;
        const data = response ? response.data : null;
        if (response.status === 500) {
          if (!data) {
            message = response.status;
          } else if (data && data.code === undefined) {
            message = data;
          } else if (data && data.code === 2012) {
            // error 2012 has the language parameter which needs to be translated
            message = t('error.' + data.code, { 0: data.params[0], 1: t('language.' + data.params[1]) });
          } else {
            message = t('error.' + data.code, data.params, {});
          }
        } else {
          message = t('error.' + data.code, data.params, {});
        }
        return Promise.reject(message);
      },
    );
  }
}

export default AxiosInterceptor;
