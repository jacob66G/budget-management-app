import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../../../core/services/auth.service';
import { ResponseMessage } from '../../../../core/models/response-message.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-recover-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatInputModule
  ],
  templateUrl: './recover-password.html',
  styleUrl: './recover-password.css'
})
export class RecoverPassword {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);

  isLoading = signal(false);
  recoverPasswordForm!: FormGroup;

  ngOnInit(): void {
    this.recoverPasswordForm = this.fb.group(
      {
        email: ['', [Validators.required, Validators.email]]
      }
    );
  }

  onSubmit(): void {
    if (this.recoverPasswordForm.invalid) {
      this.recoverPasswordForm.markAllAsTouched();
      return;
    }
    this.isLoading.set(true)

    this.authService.resetPassword(this.recoverPasswordForm.value.email).subscribe({
      next: (response: ResponseMessage) => {
        this.isLoading.set(false);
        this.snackBar.open(
            response.message,
            'OK',
            { duration: 5000, panelClass: 'success-snackbar' }
          );
      }, error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);

        let errorMessage = "An error occurred while resetting your password. Please try again later.";
        if (err.error && err.error.message) {
          errorMessage = err.error.message;
        }
        this.snackBar.open(
          errorMessage,
          'OK',
          { duration: 5000, panelClass: 'error-snackbar' }
        );
      }
    })
  }

}
