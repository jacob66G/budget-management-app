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
import { MatSnackBar } from '@angular/material/snack-bar';
import { ChangePasswordDialog } from './dialog/change-password-dialog/change-password-dialog';
import { ManageTfaDialog } from './dialog/manage-tfa-dialog/manage-tfa-dialog';
import { UserService } from '../../../../../core/services/user.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ConfirmDialog, ConfirmDialogData } from '../../../../../shared/confirm-dialog/confirm-dialog';
import { filter } from 'rxjs';
import { ResponseMessage } from '../../../../../core/models/response-message.model';


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
  private snackBar = inject(MatSnackBar)

  currentUser = this.authService.currentUser;
  isMfaEnabled = () => this.authService.currentUser()?.isMfaEnabled ?? false;

  isLoading = signal(false);

  onChangePasswordClick() {
    const dialogRef = this.dialog.open(ChangePasswordDialog, {
      width: '450px',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'success') {
        this.snackBar.open('Password has been changed!', 'OK', {
          duration: 3000,
          panelClass: 'success-snackbar'
        });
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
        this.authService.currentUser.update(user => {
          if (user) {
            return { ...user, isMfaEnabled: newStatus };
          }
          return null;
        });

        const message = newStatus ? '2FA enabled!' : '2FA disabled.';
        this.snackBar.open(message, 'OK', { duration: 3000 });
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
        this.snackBar.open(response.message, 'OK', { 
          duration: 7000, 
          panelClass: 'success-snackbar'
        });
        this.authService.logout();
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        let errorMessage = 'An error has occurred. Please try again.';
        if (err.error && err.error.message) {
          errorMessage = err.error.message;
        }
        this.snackBar.open(errorMessage, 'OK', {
          duration: 5000,
          panelClass: 'error-snackbar',
        });
      }
    });
  }
}
