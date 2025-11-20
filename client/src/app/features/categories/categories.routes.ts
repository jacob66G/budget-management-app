import { Routes } from "@angular/router";
import { CategoryPageComponent } from "./pages/category-page/category-page.component";

export const CATEGORIES_ROUTES: Routes = [
    {
        path: '',
        component: CategoryPageComponent,
        title: 'categories'
    }
]