export interface User {
  id: number;
  name?: string;
  surname?: string;
  email?: string;
  status?: string;
  mfaEnabled?: boolean;
  createdAt?: string;
  requestCloseAt?: string | null;
}
