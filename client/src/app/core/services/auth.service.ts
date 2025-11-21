import { catchError, Observable, of, tap } from "rxjs";
import { HttpClient, HttpParams } from '@angular/common/http';
import { LoginRequest, LoginResponse, PasswordResetConfirmationRequest, RegistrationRequest, TwoFactorLoginRequest } from "../../features/auth/model/auth.model";
import { ResponseMessage } from "../models/response-message.model";
import { ApiPaths } from "../../constans/api-paths";
import { Injectable, signal } from "@angular/core";
import { User } from "../models/user.model";
import { StorageKesy } from "../../constans/storage-keys";

@Injectable(
  { 'providedIn': 'root' }
)
export class AuthService {
  currentUser = signal<User | null>(this.getStoredUser());
  accessToken = signal<string | null>(this.getStoredToken());

  constructor(private http: HttpClient) { }

  register(registrationData: RegistrationRequest): Observable<ResponseMessage> {
    return this.http.post<ResponseMessage>(ApiPaths.Auth.REGISTER, registrationData);
  }

  login(loginData: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(ApiPaths.Auth.LOGIN, loginData, { withCredentials: true }).pipe(
      tap((response) => {
        if (!response.isMfaRequired && response.accessToken) {
          const user = this.buildUser(response);
          this.setAuthState(response.accessToken, user);
        }
      })
    )
  }

  loginWith2FA(twoFactorData: TwoFactorLoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(ApiPaths.Auth.LOGIN_2FA, twoFactorData, { withCredentials: true }).pipe(
      tap((response) => {
        if (response.accessToken) {
          const user = this.buildUser(response);
          this.setAuthState(response.accessToken, user);
        }
      })
    );
  }

  logout(): Observable<ResponseMessage> {
    return this.http.post<ResponseMessage>(ApiPaths.Auth.LOGOUT, { withCredentials: true }).pipe(
      tap(() => {
        this.clearAuthState();
      })
    );
  }

  refreshToken(): Observable<LoginResponse | null> {
    return this.http.post<LoginResponse>(
      ApiPaths.Auth.REFRESH_TOKEN, { withCredentials: true }).pipe(
        tap((response) => {
          if (response.accessToken) {
            const user = this.buildUser(response);
            this.setAuthState(response.accessToken, user);
          }
        }),
        catchError(() => {
          return of(null);
        })
      );
  }

  verifyEmail(code: string): Observable<void> {
    let params = new HttpParams().set('code', code);
    return this.http.get<void>(ApiPaths.Auth.VERIFY_EMAIL, { params });
  }

  resendVerificationEmail(email: string): Observable<ResponseMessage> {
    return this.http.post<ResponseMessage>(ApiPaths.Auth.RESEND_VERIFICATION, { email });
  }

  resetPassword(email: string): Observable<ResponseMessage> {
    return this.http.post<ResponseMessage>(ApiPaths.Auth.RESET_PASSWORD_REQUEST, { email });
  }

  resetPasswordConfirm(resetPasswordData: PasswordResetConfirmationRequest): Observable<ResponseMessage> {
    return this.http.post<ResponseMessage>(ApiPaths.Auth.RESET_PASSWORD_CONFIRM, resetPasswordData);
  }

  patchCurrentUser(partialUser: Partial<User>): void {
    const current = this.currentUser();
    if (current) {
      const updatedUser = { ...current, ...partialUser };
      this.updateCurrentUser(updatedUser);
    }
  }

  updateCurrentUser(user: User): void {
    this.currentUser.set(user);
    localStorage.setItem(StorageKesy.USER_KEY, JSON.stringify(user));
  }

  private getStoredToken(): string | null {
    return localStorage.getItem(StorageKesy.TOKEN_KEY);
  }

  private getStoredUser(): User | null {
    const userStr = localStorage.getItem(StorageKesy.USER_KEY);
    try {
      return userStr ? JSON.parse(userStr) : null;
    } catch (e) {
      return null;
    }
  }

  private setAuthState(token: string, user: User): void {
    this.accessToken.set(token);
    this.currentUser.set(user);

    localStorage.setItem(StorageKesy.TOKEN_KEY, token);
    localStorage.setItem(StorageKesy.USER_KEY, JSON.stringify(user));
  }

  private clearAuthState(): void {
    this.currentUser.set(null);
    this.accessToken.set(null);

    localStorage.removeItem(StorageKesy.TOKEN_KEY);
    localStorage.removeItem(StorageKesy.USER_KEY);
  }

  private buildUser(response: LoginResponse): User {
    const user: User = {
      id: response.userId,
      name: response.name,
      surname: response.surname,
      email: response.email,
      status: response.status,
      mfaEnabled: response.mfaEnabled,
      createdAt: response.createdAt,
      requestCloseAt: null
    };

    return user;
  }

}
