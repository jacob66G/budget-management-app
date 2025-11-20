import { Routes } from '@angular/router';
import { HomePageComponent } from './pages/home-page/home-page.component';

export const Home_ROUTES: Routes = [
    {
        path: '',
        component: HomePageComponent,
        title: 'Home'
    }
]