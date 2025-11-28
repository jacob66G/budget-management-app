import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../../core/services/auth.service';
import { passwordsMatchValidator } from '../registration/registration';
import { PasswordResetConfirmationRequest } from '../../model/auth.model';
import { ApiErrorService } from '../../../../core/services/api-error.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.scss'
})
export class ResetPassword {
  private fb = inject(FormBuilder)
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  private errorService = inject(ApiErrorService); 

  isLoading = signal(false);
  hidePassword = signal(true);

  resetPasswordForm!: FormGroup;
  private token: string | null = null;

  constructor() {
    this.resetPasswordForm = this.fb.group(
      {
        newPassword: ['', [Validators.required, Validators.minLength(5)]],
        passwordConfirmation: ['', [Validators.required]],
      },
      {
        validators: passwordsMatchValidator('newPassword', 'passwordConfirmation'),
      }
    );

    this.token = this.route.snapshot.queryParamMap.get('code');
  }

  ngOnInit(): void {
    if (!this.token) {
      this.snackBar.open('Invalid or missing password reset token.', 'OK', {
        duration: 5000,
        panelClass: 'error-snackbar',
      });
      this.router.navigate(['/login']);
    }
  }

  onSubmit(): void {
    if (this.resetPasswordForm.invalid || !this.token) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const dto: PasswordResetConfirmationRequest = {
      token: this.token,
      newPassword: this.resetPasswordForm.value.newPassword,
      confirmedNewPassword: this.resetPasswordForm.value.passwordConfirmation,
    };

    this.authService.resetPasswordConfirm(dto).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackBar.open('Your password has been successfully changed!', 'OK', {
          duration: 5000,
          panelClass: 'success-snackbar',
        });

        this.router.navigate(['/login']);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err);
      },
    });
  }

}
