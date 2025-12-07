import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { Router } from '@angular/router';
import { TwoFactorLoginRequest } from '../../model/auth.model';
import { HttpErrorResponse } from '@angular/common/http';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiErrorService } from '../../../../core/services/api-error.service';

@Component({
  selector: 'app-login2fa',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login2fa.html',
  styleUrl: './login2fa.scss'
})
export class Login2fa {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private errorService = inject(ApiErrorService);

  isLoading = signal(false);
  tfaForm!: FormGroup;

  private userId: number | null = null;

  constructor() {
    const navigation = this.router.currentNavigation();
    this.userId = navigation?.extras?.state?.['userId']
  }

  ngOnInit(): void {
    this.tfaForm = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
    });

     if (!this.userId) {
      this.router.navigate(['/login']);
    }
  }

  onSubmit(): void {
    if (this.tfaForm.invalid) {
      this.tfaForm.markAllAsTouched();
      return;
    }

    if (!this.userId) return;

    this.isLoading.set(true);

    const payload: TwoFactorLoginRequest = {
      userId: this.userId,
      code: this.tfaForm.value.code
    };

    this.authService.loginWith2FA(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackBar.open('Login successful', 'OK', { duration: 3000 });
        this.router.navigate(['/app/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        let errorMessage = 'Verification 2FA failed. Try again.';
        if (err.error && err.error.message) {
            errorMessage = err.error.message;
        } else if (err.status === 401) {
            errorMessage = 'Incorrect code.';
        }

        this.errorService.handle(err, errorMessage);
      }
    });
  }
}
