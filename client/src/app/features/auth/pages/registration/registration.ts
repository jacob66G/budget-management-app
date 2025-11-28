import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import {RegistrationRequest} from '../../model/auth.model';
import { AuthService } from '../../../../core/services/auth.service';
import { ApiErrorService } from '../../../../core/services/api-error.service';


export function passwordsMatchValidator(
  controlName: string,
  matchingControlName: string
): ValidatorFn {

  return (control: AbstractControl): ValidationErrors | null => {

    const password = control.get(controlName);
    const passwordConfirmation = control.get(matchingControlName);

    if (
      password &&
      passwordConfirmation &&
      password.value !== passwordConfirmation.value
    ) {
      return { passwordsNotMatching: true };
    }

    return null;
  };
}

@Component({
  selector: 'app-registration',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './registration.html',
  styleUrl: './registration.scss',
})
export class Registration {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private errorService = inject(ApiErrorService); 

  isLoading = signal(false);
  hidePassword = signal(true);
  registerForm!: FormGroup;

  ngOnInit(): void {
     this.registerForm = this.fb.group(
      {
        name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(30)]],
        surname: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(30)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(5)]],
        passwordConfirmation: ['', [Validators.required]],
      },
      {
        validators: passwordsMatchValidator('password', 'passwordConfirmation'),
      }
    );
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const dto = this.registerForm.value as RegistrationRequest;

    this.authService.register(dto).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackBar.open(
          'Registration successful! Check your email to activate your account.',
          'OK',
          { duration: 5000, panelClass: 'success-snackbar' }
        );

        this.router.navigate(['/verifi-pending'], {
          queryParams: { email: dto.email }
        });
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err, 'Registration failed. Please try again.');
      },
    });
  }

}
