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
  userId: number;
  name: string;
  surname: string;
  email: string;
  status: string;
  mfaEnabled: boolean;
  createdAt: string;
  accessToken: string;
  isMfaRequired: boolean;
}

export interface PasswordResetConfirmationRequest {
  token: string;
  newPassword: string;
  confirmedNewPassword: string;
}
