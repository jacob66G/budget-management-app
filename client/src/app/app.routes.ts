import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        loadChildren: () =>
        import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
    },
    {
        path: 'app',
        loadChildren: () =>
        import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
    },
     {
        path: 'profile',
        loadChildren: () =>
        import('./features/user/user-profile.routes').then((m) => m.USER_PROFILE_ROUTE),
    }
];
