import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';
import { ApiPaths } from '../../constans/api-paths';
import { LoginResponse } from '../../features/auth/model/auth.model';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.accessToken();

  const excludedUrls = [
    ApiPaths.Auth.LOGOUT,
    ApiPaths.Auth.LOGIN,
    ApiPaths.Auth.LOGIN_2FA,
    ApiPaths.Auth.REGISTER,
    ApiPaths.Auth.REFRESH_TOKEN,
    ApiPaths.Auth.VERIFY_EMAIL,
    ApiPaths.Auth.RESEND_VERIFICATION,
    ApiPaths.Auth.RESET_PASSWORD_REQUEST,
    ApiPaths.Auth.RESET_PASSWORD_CONFIRM,
    ApiPaths.ReferenceData.REFERENCE_DATA
  ]

  const isPublicEndpoint = excludedUrls.some(url => req.url.includes(url));

  let authReq = req;
  if (token && !isPublicEndpoint) {
    authReq = addTokenToRequest(req, token);
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {

        if (req.url.includes(ApiPaths.Auth.REFRESH_TOKEN)) {
          return throwError(() => error);
        }

        if (isPublicEndpoint) {
          return throwError(() => error);
        }

        return authService.refreshToken().pipe(
          switchMap((response: LoginResponse | null) => {
            if (response?.accessToken) {
              authReq = addTokenToRequest(req, response.accessToken);
              return next(authReq);
            }
            authService.logout().subscribe();
            return throwError(() => error);
          }),
          catchError((refreshError) => {
            authService.logout().subscribe();
            return throwError(() => refreshError);
          })
        );
      }

      return throwError(() => error);
    })
  );
};

function addTokenToRequest(req: HttpRequest<any>, token: string): HttpRequest<any> {
  return req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });
}
