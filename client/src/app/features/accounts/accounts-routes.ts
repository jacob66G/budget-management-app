import { Routes } from "@angular/router";

export const ACCOUNTS_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => 
            import('./pages/accounts-list/accounts-list').then((m) => m.AccountsListPage),
        title: 'Accounts'
    },
    {
        path: 'create',
        loadComponent: () =>
            import('./pages/account-add.page/account-add.page').then((m) => m.AccountAddPage),
        title: "Create Account"
    },
    {
        path: ':id',
        loadComponent: () =>
            import('./pages/account-details/account-details').then((m) => m.AccountDetailsPage),
        title: 'Account Details'
    },
    {
        path: 'edit/:id',
        loadComponent: () => 
            import('./pages/account-edit/account-edit.page').then((m) => m.AccountEditPage),
        title: 'Edit Account'
    },
    
]