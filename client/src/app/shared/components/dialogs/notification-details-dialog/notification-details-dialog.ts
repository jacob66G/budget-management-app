import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { NotificationDto } from '../../../../core/models/notification.model';

@Component({
  selector: 'app-notification-details-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule, DatePipe],
  templateUrl: './notification-details-dialog.html',
  styleUrl: './notification-details-dialog.scss'
})
export class NotificationDetailsDialog {
  public data: NotificationDto = inject(MAT_DIALOG_DATA)
  private dialogRef = inject(MatDialogRef<NotificationDetailsDialog>);
  private router = inject(Router);

  getIcon(type: string): string {
    switch (type) {
      case 'ERROR': return 'error';
      case 'SUCCESS': return 'check_circle';
      case 'WARNING': return 'warning';
      default: return 'info';
    }
  }

  navigateToTarget(): void {
    if (this.data.targetUrl) {
      this.router.navigateByUrl(this.data.targetUrl);
      this.dialogRef.close();
    }
  }
}
