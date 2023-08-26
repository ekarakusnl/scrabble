export interface UserProfile {
  id: number;
  username: string;
  password: string;
  email: string;
  preferredLanguage?: string;
}