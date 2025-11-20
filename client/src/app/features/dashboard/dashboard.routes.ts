import { Routes } from "@angular/router";
import { Dashboard } from "./dashboard";

export const DASHBOARD_ROUTES: Routes = [
    {
        path: '',
        component: Dashboard,
        children: [
            {
                path: '',
                redirectTo: 'home',
                pathMatch: 'full'
            },
            {
                path: 'home',
                loadChildren: () =>
                    import('../home/home.routes').then((m) => m.Home_ROUTES),
            },
            {
                path: 'transactions',
                loadChildren: () =>
                    import('../transactions/transactions-routes').then( (m) => m.TRANSACTIONS_ROUTES),
            },
            {
                path: 'accounts',
                loadChildren: () =>
                    import('../accounts/accounts-routes').then((m) => m.ACCOUNTS_ROUTES),
            },
            {
                path: 'categories',
                loadChildren: () =>
                    import('../categories/categories.routes').then((m) => m.CATEGORIES_ROUTES),
            }
        ]
    }
]
