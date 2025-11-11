export interface User {
  userId: number;
  name?: string;
  surname?: string;
  email?: string;
  status?: string;
  isMfaEnabled?: boolean;
  createdAt?: string;
}
