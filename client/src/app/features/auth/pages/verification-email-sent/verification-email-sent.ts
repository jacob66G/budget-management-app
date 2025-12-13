import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ResponseMessage } from '../../../../core/models/response-message.model';
import { HttpErrorResponse } from '@angular/common/http';

import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { ApiErrorService } from '../../../../core/services/api-error.service';
import { ToastService } from '../../../../core/services/toast-service';


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
  styleUrl: './verification-email-sent.scss'
})
export class VerificationEmailSent {
  private authService = inject(AuthService)
  private toastService = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private errorService = inject(ApiErrorService); 

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
      this.toastService.showError('No email address provided.');
      return;
    }

    this.isLoading.set(true);

    this.authService.resendVerificationEmail(currentEmail).subscribe({
      next: (response: ResponseMessage) => {
        this.isLoading.set(false);
           this.toastService.showSuccess(response.message);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err, 'Action failed. Try again.')
      }
    });
  }
}
