import { Routes } from '@angular/router';
import { MainLayout } from './core/layout/main-layout/main-layout';
import { AuthGuard } from './core/guards/auth.guard';
import { PublicGuard } from './core/guards/public-guard';

export const routes: Routes = [
  {
    path: '', 
    canActivate: [PublicGuard],
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES),
  },
  {
    path: 'app',
    canActivate: [AuthGuard],
    component: MainLayout,
    children: [
      {
        path: 'dashboard',
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
      },
      {
        path: 'profile',
        loadChildren: () =>
          import('./features/user/user-profile.routes').then((m) => m.USER_PROFILE_ROUTE),
      },
      {
        path: 'accounts',
        loadChildren: () => import('./features/accounts/accounts-routes').then(m => m.ACCOUNTS_ROUTES)
      },
      {
        path: 'categories',
        loadChildren: () => import('./features/categories/categories.routes').then(m => m.CATEGORIES_ROUTES)
      },
       {
        path: 'transactions',
        loadChildren: () => import('./features/transactions/transactions-routes').then(m => m.TRANSACTIONS_ROUTES)
      },
    ]
  }
];
