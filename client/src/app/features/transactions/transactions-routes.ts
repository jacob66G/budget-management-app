import { Routes } from "@angular/router";
import { TransactionPageComponent } from "./pages/transaction-page/transaction-page.component";
import { RecurringTemplatesPageComponent } from "./pages/recurring-templates-page/recurring-templates-page.component";

export const TRANSACTIONS_ROUTES: Routes = [
    {
        path: '',
        component: TransactionPageComponent,
        title: 'transactions'
    }, {
        path: 'recurring-templates',
        component: RecurringTemplatesPageComponent,
        title: 'recurring-templates'
    }
]