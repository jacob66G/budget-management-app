import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from "@angular/material/card";
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../../../core/services/auth.service';
import { UserService } from '../../../../../core/services/user.service';
import { HttpErrorResponse } from '@angular/common/http';
import { filter } from 'rxjs';
import { ResponseMessage } from '../../../../../core/models/response-message.model';
import { ChangePasswordDialog } from './dialogs/change-password-dialog/change-password-dialog';
import { ManageTfaDialog } from './dialogs/manage-tfa-dialog/manage-tfa-dialog';
import { ConfirmDialog, ConfirmDialogData } from '../../../../../shared/components/dialogs/confirm-dialog/confirm-dialog';
import { ApiErrorService } from '../../../../../core/services/api-error.service';
import { ToastService } from '../../../../../core/services/toast-service';


@Component({
  selector: 'app-security-settings',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './security-settings.html',
  styleUrl: './security-settings.scss'
})
export class SecuritySettings {

  private userService = inject(UserService)
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private toastService = inject(ToastService);
  private errorService = inject(ApiErrorService);

  currentUser = this.authService.currentUser;
  isMfaEnabled = () => this.authService.currentUser()?.mfaEnabled ?? false;

  isLoading = signal(false);

  onChangePasswordClick() {
    const dialogRef = this.dialog.open(ChangePasswordDialog, {
      width: '450px',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'success') {
        this.toastService.showSuccess('Password changed successfully');
      }
    });
  }

  onManageMfaClick(): void {
    const dialogRef = this.dialog.open(ManageTfaDialog, {
      width: '500px',
      data: {
        isMfaEnabled: this.isMfaEnabled()
      }
    });

    dialogRef.afterClosed().subscribe(newStatus => {
      if (typeof newStatus === 'boolean') {
        const isMfaEnabled = newStatus;

        this.authService.patchCurrentUser({ mfaEnabled: isMfaEnabled });

        const message = newStatus ? '2FA enabled!' : '2FA disabled.';
        this.toastService.showSuccess(message);
      }
    });

  }

  closeAccount(): void {
    const dialogData: ConfirmDialogData = {
      title: 'Close your account?',
      message: 'Are you sure you want to close your account? All your data will be marked for deletion in 30 days. You can cancel this process by logging in again during this period.',
      confirmButtonText: 'Yes, close my account',
      confirmButtonColor: 'warn'
    };

    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: dialogData,
      width: '450px',
      autoFocus: false
    });

    dialogRef.afterClosed()
      .pipe(
        filter(result => result === true)
      )
      .subscribe(() => {
        this.executeCloseAccount();
      });
  }


  private executeCloseAccount(): void {
    this.isLoading.set(true);

    this.userService.closeAccount().subscribe({
      next: (response: ResponseMessage) => {
        this.isLoading.set(false);
        this.toastService.showSuccess(response.message);
        this.authService.logout();
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err);
      }
    });
  }
}
