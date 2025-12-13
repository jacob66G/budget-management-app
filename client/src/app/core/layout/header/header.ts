import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { NotificationService } from '../../services/notification.service';
import { NotificationDto } from '../../models/notification.model';
import { MatDialog } from '@angular/material/dialog';
import { NotificationDetailsDialog } from '../../../shared/components/dialogs/notification-details-dialog/notification-details-dialog';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
    MatBadgeModule,
    MatDividerModule
  ],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class Header {
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);
  private router = inject(Router);
  private dialog = inject(MatDialog);

  currentUser = this.authService.currentUser;

  unreadCount = this.notificationService.unreadCount; 
  notifications = this.notificationService.notifications;

  onNotificationClick(notification: NotificationDto): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id);
    }

    this.dialog.open(NotificationDetailsDialog, {
      width: '450px',
      data: notification,
      autoFocus: false
    });
  }


  onMarkAllAsRead(event: Event): void {
    event.stopPropagation();
    this.notificationService.markAllAsRead();
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      }
    });
  }

  onProfileClick(): void {
    this.router.navigate(['/app/profile']);
  }

  getIconName(type: string): string {
      switch(type) {
          case 'ERROR': return 'error';
          case 'SUCCESS': return 'check_circle';
          case 'WARNING': return 'warning';
          default: return 'info';
      }
  }
}
