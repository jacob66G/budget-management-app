import { Routes } from "@angular/router";
import { UserProfile } from "./user-profile/user-profile";

export const USER_PROFILE_ROUTE: Routes = [
    {
        path: '',
        component: UserProfile,
        children: [
            {
                path: 'details',
                loadComponent: () => import('./user-profile/pages/profile-details/profile-details').then(m => m.ProfileDetails),
                title: 'Profile Details'
            },
            {
                path: 'security',
                loadComponent: () => import('./user-profile/pages/security-settings/security-settings').then(m => m.SecuritySettings),
                title: 'Security Settings'
            },
            {
                path: 'sessions',
                loadComponent: () => import('./user-profile/pages/session-settings/session-settings').then(m => m.SessionSettings),
                title: 'Session Settings'
            },
            {
                path: '',
                redirectTo: 'details',
                pathMatch: 'full',
            },
        ]
    }
]