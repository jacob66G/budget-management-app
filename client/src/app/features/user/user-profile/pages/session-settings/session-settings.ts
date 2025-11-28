import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserService } from '../../../../../core/services/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserSession } from '../../model/user-profile.model';
import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { ApiErrorService } from '../../../../../core/services/api-error.service';

@Component({
  selector: 'app-session-settings',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './session-settings.html',
  styleUrl: './session-settings.scss'
})
export class SessionSettings {
  private userService = inject(UserService);
  private errorService = inject(ApiErrorService);
  private snackBar = inject(MatSnackBar);

  sessions = signal<UserSession[]>([]);
  isRevoking = signal<number | null>(null);
  isRevokingAll = signal(false);
  isLoading = signal(false);

  ngOnInit(): void {
    this.isLoading.set(true)
    this.userService.getSessions().subscribe({
      next: (response: UserSession[]) => {
        this.sessions.set(response.sort((a, b) => (a.isCurrent ? -1 : 1)));
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err);
      }
    })
  }

  onRevoke(sessionId: number): void {
    this.isRevoking.set(sessionId);
    this.userService.revokeSession(sessionId).subscribe({
      next: () => {
        this.sessions.update(s => s.filter(session => session.id !== sessionId));
        this.isRevoking.set(null);
        this.snackBar.open('The device has been logged out.', 'OK', { duration: 3000 });
      },
      error: (err: HttpErrorResponse) => {
        this.isRevoking.set(null);
        this.errorService.handle(err);
      }
    });
  }

  // onRevokeAll(): void {
  //   const dialogData: ConfirmDialogData = {
  //     title: 'Reveoke all sessions?',
  //     message: 'Are you sure you want revoke all sessions?. This action will also revoke the current session and log you out',
  //     confirmButtonText: 'Yes, revoke',
  //     confirmButtonColor: 'warn'
  //   };

  //   const dialogRef = this.dialog.open(ConfirmDialog, {
  //     data: dialogData,
  //     width: '450px',
  //     autoFocus: false
  //   });

  //   dialogRef.afterClosed()
  //     .pipe(
  //       filter(result => result === true)
  //     )
  //     .subscribe(() => {
  //       this.revokeAllSessions();
  //     });
  // }

  // private revokeAllSessions() {
  //   this.isRevokingAll.set(true);
  //   this.userService.revokeAllSessions().subscribe({
  //     next: () => {
  //       this.isRevokingAll.set(false);
  //       this.snackBar.open('All session have been revoked.', 'OK', { duration: 3000 });
  //       this.authService.logout().subscribe({
  //         next: () => {
  //           this.router.navigate(['/login']);
  //         }
  //       })
  //     },
  //     error: (err: HttpErrorResponse) => {
  //       this.isRevokingAll.set(false);
  //       this.showError(err);
  //     }
  //   });
  // }
}
