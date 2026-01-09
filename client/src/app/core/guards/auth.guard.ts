import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map } from 'rxjs';

export const AuthGuard: CanActivateFn = (route, state) => {
  const authService =  inject(AuthService);
  const router = inject(Router)

  return authService.checkAuth().pipe(
    map(isAuthenticated => {
      if (isAuthenticated) {
                console.log('Not authenticated, redirecting to login');

        return true;
      } else {
        console.log('Not authenticated, redirecting to login');
        return router.createUrlTree(['/login']);
      }
    })
  );
};
