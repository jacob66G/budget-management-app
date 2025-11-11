import { catchError, Observable, of, tap } from "rxjs";
import { HttpClient, HttpParams } from '@angular/common/http';
import { LoginRequest, LoginResponse, PasswordResetConfirmationRequest, RegistrationRequest, TwoFactorLoginRequest } from "../../features/auth/model/auth.model";
import { ResponseMessage } from "../models/response-message.model";
import { ApiPaths } from "../../constans/api-paths";
import { Injectable, signal } from "@angular/core";
import { User } from "../models/user.model";

@Injectable(
    {'providedIn': 'root'}
)
export class AuthService {
    currentUser = signal<User | null>(null);
    accessToken = signal<string | null>(null);

    constructor(private http: HttpClient) {}

    register(registrationData: RegistrationRequest): Observable<ResponseMessage> {
        return this.http.post<ResponseMessage>(ApiPaths.REGISTER, registrationData);
    }

    login(loginData: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(ApiPaths.LOGIN, loginData).pipe(
            tap((response) => {
                if (!response.isMfaRequired && response.accessToken && response.user) {
                    this.setAuthState(response.user, response.accessToken);
                }
            })
        )
    }

    loginWith2FA(twoFactorData: TwoFactorLoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(ApiPaths.LOGIN_2FA, twoFactorData).pipe(
            tap((response) => {
                if (response.accessToken && response.user) {
                    this.setAuthState(response.user, response.accessToken);
                }
            })
        );
    }

    logout(): Observable<ResponseMessage> {
        return this.http.post<ResponseMessage>(ApiPaths.LOGOUT, {}).pipe(
            tap(() => {
                this.clearAuthState();
            })
        );
    }

    refreshToken(): Observable<LoginResponse | null> { 
        return this.http.post<LoginResponse>(
        ApiPaths.REFRESH_TOKEN, {}, { withCredentials: true }).pipe(
            tap((response) => {
                if (response.accessToken && response.user) {
                this.setAuthState(response.user, response.accessToken);
                }
            }),
            catchError(() => {
                return of(null); 
            })
        );
    }

    verifyEmail(code: string): Observable<void> {
        let params = new HttpParams().set('code', code);
      return this.http.get<void>(ApiPaths.VERIFY_EMAIL, { params });
    }

    resendVerificationEmail(email: string): Observable<ResponseMessage> {
        return this.http.post<ResponseMessage>(ApiPaths.RESEND_VERIFICATION_EMAIL, { email });
    }

    resetPassword(email: string): Observable<ResponseMessage> {
        return this.http.post<ResponseMessage>(ApiPaths.RESET_PASSWORD, { email });
    }

    resetPasswordConfirm(resetPasswordData: PasswordResetConfirmationRequest): Observable<ResponseMessage> {
        return this.http.post<ResponseMessage>(ApiPaths.REST_PASSWORD_CONFIRM, resetPasswordData);
    }

    private setAuthState(user: User, accessToken: string): void {
        this.currentUser.set(user);
        this.accessToken.set(accessToken);
    }

    private clearAuthState(): void {
        this.currentUser.set(null);
        this.accessToken.set(null);
    }

}

