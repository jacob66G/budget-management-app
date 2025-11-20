import { Routes } from "@angular/router";
import { TransactionPageComponent } from "./pages/transaction-page/transaction-page.component";

export const TRANSACTIONS_ROUTES: Routes = [
    {
        path: '',
        component: TransactionPageComponent,
        title: 'transactions'
    }
]