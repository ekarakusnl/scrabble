export interface UserToken {
  id: number;
  token: string;
  roles: string[];
  preferredLanguage: string;
}