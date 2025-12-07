import { Routes } from "@angular/router";

export const DASHBOARD_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => 
            import('./pages/dashboard.page').then((m) => m.DashboardPage),
        title: 'Dashboard'
    }
]
