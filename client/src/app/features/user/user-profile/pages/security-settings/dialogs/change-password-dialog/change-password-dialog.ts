import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../../../../../core/services/user.service';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { passwordsMatchValidator } from '../../../../../../auth/pages/registration/registration';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangePasswordRequest } from '../../../../model/user-profile.model';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiErrorService } from '../../../../../../../core/services/api-error.service';

@Component({
  selector: 'app-change-password-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './change-password-dialog.html',
  styleUrl: './change-password-dialog.scss'
})
export class ChangePasswordDialog {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private errorService = inject(ApiErrorService)

  public dialogRef = inject(MatDialogRef<ChangePasswordDialog>);

  changePasswordForm!: FormGroup;
  isLoading = signal(false);
  hidePassword = signal(true);

  ngOnInit(): void {
    this.changePasswordForm = this.fb.group(
      {
        oldPassword: ['', [Validators.required]],
        newPassword: ['', [Validators.required, Validators.minLength(5)]],
        passwordConfirmation: ['', [Validators.required]],
      },
      {
        validators: passwordsMatchValidator('newPassword', 'passwordConfirmation'),
      }
    );
  }

  onSubmit(): void {
    if (this.changePasswordForm.invalid) {
      this.changePasswordForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const dto = this.changePasswordForm.value as ChangePasswordRequest;

    this.userService.changePassword(dto).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.dialogRef.close('success');
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err);
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
