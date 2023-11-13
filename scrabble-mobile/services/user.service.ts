import axios from 'axios';

import { User } from '../model/user';
import { ImagePickerAsset } from 'expo-image-picker';

const UserService = {
  async create(username: string, email: string, password: string): Promise<void> {
    return axios.post('/signup',
      JSON.stringify({
        username: username,
        email: email,
        password: password,
      }),
      { headers: { 'Content-Type': 'application/json', } }
    );
  },
  async update(username: string, email: string, password: string, preferredLanguage: string): Promise<void> {
    return axios.put('/rest/users',
      JSON.stringify({
        username: username,
        email: email,
        password: password,
        preferredLanguage: preferredLanguage,
      }),
      { headers: { 'Content-Type': 'application/json', } }
    );
  },
  async get(): Promise<User> {
    return axios.get('/rest/users/authenticated');
  },
  async updateProfilePicture(profilePictureURI: string): Promise<void> {
    const formData: FormData = new FormData();
    let filename = profilePictureURI.split('/').pop();

    // type of the image
    let match = /\.(\w+)$/.exec(filename);
    let type = match ? `image/${match[1]}` : `image`;

    formData.append('file',{
      uri: profilePictureURI,
      name: filename,
      type
    });
    return axios.post('/rest/images/profile', formData);
  },
}

export default UserService;