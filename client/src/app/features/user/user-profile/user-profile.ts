import { Component } from '@angular/core';

import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ProfileDetails } from "./pages/profile-details/profile-details";
import { SecuritySettings } from "./pages/security-settings/security-settings";
import { SessionSettings } from "./pages/session-settings/session-settings";

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [
    MatTabsModule,
    MatCardModule,
    MatIconModule,
    ProfileDetails,
    SecuritySettings,
    SessionSettings
],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.scss'
})
export class UserProfile {
    
}
