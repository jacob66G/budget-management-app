import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ResponseMessage } from '../../../../core/models/response-message.model';
import { HttpErrorResponse } from '@angular/common/http';

import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';


@Component({
  selector: 'app-verification-email-sent',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIcon
  ],
  templateUrl: './verification-email-sent.html',
  styleUrl: './verification-email-sent.css'
})
export class VerificationEmailSent {
  private authService = inject(AuthService)
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private route = inject(ActivatedRoute); 

  isLoading = signal(false);
  email = signal<string | null>(null);

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const emailFromParams = params.get('email');
      if (emailFromParams) {
        this.email.set(emailFromParams);
      } else {
        this.router.navigate(['/login']);
      }
    });
  }

  onResendVerificationEmail(): void {
    const currentEmail = this.email();
    if (!currentEmail) {
      this.snackBar.open('No email address', 'OK', {
        duration: 3000,
        panelClass: 'error-snackbar',
      });
      return;
    }

    this.isLoading.set(true);

    this.authService.resendVerificationEmail(currentEmail).subscribe({
      next: (response: ResponseMessage) => {
        this.isLoading.set(false);
           this.snackBar.open(
            response.message,
            'OK',
            { duration: 5000, panelClass: 'success-snackbar' }
          );
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);

        let errorMessage = 'Action failed. Try again.';
        if (err.error && err.error.message) {
          errorMessage = err.error.message;
        } 

       this.snackBar.open(
          errorMessage,
          'OK',
          { duration: 5000, panelClass: 'error-snackbar' }
        );
      }
    });
  }
}
