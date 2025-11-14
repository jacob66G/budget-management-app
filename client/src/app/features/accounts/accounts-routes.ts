import { Routes } from "@angular/router";
import { AccountPageComponent } from "./pages/account-page/account-page.component";

export const ACCOUNTS_ROUTES: Routes = [
    {
        path: '',
        component: AccountPageComponent,
        title: 'accounts'
    }
]