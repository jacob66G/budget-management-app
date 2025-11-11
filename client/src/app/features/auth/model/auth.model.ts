import { User } from "../../../core/models/user.model";

export interface RegistrationRequest {
  name: string;
  surname: string;
  email: string;
  password: string;
  passwordConfirmation: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TwoFactorLoginRequest {
  userId: number;
  code: string;
}

export interface LoginResponse {
  user?: User;
  accessToken?: string | null;
  isMfaRequired: boolean;
}

export interface PasswordResetConfirmationRequest {
  token: string;
  newPassword: string;
  confirmedNewPassword: string;
}
