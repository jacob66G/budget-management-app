import { Injectable, inject } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { NotificationType } from "../models/notification.model";

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private snackBar = inject(MatSnackBar);

  showSuccess(message: string): void {
    this.snackBar.open(message, 'OK', {
      duration: 4000,
      panelClass: 'success-snackbar',
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }

  showError(message: string): void {
    this.snackBar.open(`⚠️ ${message}`, 'CLOSE', {
      duration: 5000,
      panelClass: 'error-snackbar',
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }

  showNotification(title: string, message: string, type: NotificationType = NotificationType.INFO): void {
    let panelClass = 'info-snackbar';

    switch (type) {
      case NotificationType.SUCCESS: panelClass = 'success-snackbar'; break;
      case NotificationType.ERROR: panelClass = 'error-snackbar'; break;
      case NotificationType.WARNING: panelClass = 'warning-snackbar'; break;
      case NotificationType.INFO: panelClass = 'info-snackbar'; break;
    }

    this.snackBar.open(`${title}: ${message}`, 'OK', {
      duration: 5000,
      panelClass: panelClass,
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }
}