import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LoginRequest, LoginResponse } from '../../model/auth.model';
import { HttpErrorResponse } from '@angular/common/http';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiErrorService } from '../../../../core/services/api-error.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private errorService = inject(ApiErrorService);
  
  isLoading = signal(false);
  hidePassword = signal(true);
  loginForm!: FormGroup;

  constructor() {}

  ngOnInit(): void {
    this.loginForm = this.fb.group(
      {
        email: ['', [Validators.required, Validators.email]],
        password: ['', Validators.required],
      }
    );
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.isLoading.set(true)
    const dto = this.loginForm.value as LoginRequest;

    this.authService.login(dto).subscribe({
      next: (response: LoginResponse) => {
        this.isLoading.set(false);

        if (response.isMfaRequired) {
          this.router.navigate(['/login/2fa'], {
            state: { userId: response.userId }
          });
        } else {
          this.snackBar.open(
            'Login successful',
            'OK',
            { duration: 5000, panelClass: 'success-snackbar' }
          );
           this.router.navigate(['/app']);
        }
      }, error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err, 'Log in failed. Try again.')
      }
    });
  }

}
