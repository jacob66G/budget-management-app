import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';
import { ApiPaths } from '../../constans/api-paths';
import { LoginResponse } from '../../features/auth/model/auth.model';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.accessToken();

  let authReq = req;
  if (token) {
    authReq = addTokenToRequest(req, token);
  }
  
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {

        if (req.url === ApiPaths.REFRESH_TOKEN) {
          authService.logout();
          return throwError(() => error);
        }
      

      return authService.refreshToken().pipe(
        switchMap((response: LoginResponse | null) => {
          if (response?.accessToken) {
            authReq = addTokenToRequest(req, response.accessToken);
            return next(authReq);
          }
            return throwError(() => error);
          }),
          catchError((refreshError) => {
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
