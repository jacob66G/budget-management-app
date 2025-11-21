import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../../../../../core/services/user.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TfaQRCode, TfaVerifyRequest } from '../../../../model/user-profile.model';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-manage-tfa-dialog',
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
    MatSnackBarModule
  ],
  templateUrl: './manage-tfa-dialog.html',
  styleUrl: './manage-tfa-dialog.scss'
})
export class ManageTfaDialog {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private snackBar = inject(MatSnackBar);
  public dialogRef = inject(MatDialogRef<ManageTfaDialog>);
  public data: { isMfaEnabled: boolean } = inject(MAT_DIALOG_DATA);

  isLoading = signal(false);
  isQrLoading = signal(false);

  qrCodeUrl = signal<string | null>(null);

  tfaForm!: FormGroup;

  ngOnInit(): void {
    this.tfaForm = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
    });

    if (!this.data.isMfaEnabled) {
      this.fetchQrCode();
    }
  }

  fetchQrCode(): void {
    this.isQrLoading.set(true);

    this.userService.tfaSetup().subscribe({
      next: (response: TfaQRCode) => {
        this.qrCodeUrl.set(response.secretImageUri);
        this.isQrLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isQrLoading.set(false);

        let errorMessage = "An error occured. Please try again";

        if (err.error && err.error.message) {
          errorMessage = err.error.message;
        }

        this.snackBar.open(errorMessage, 'OK', {
          duration: 5000,
          panelClass: 'error-snackbar',
        });
        this.dialogRef.close();
      }
    });
  }

  onSubmit(): void {
    if (this.tfaForm.invalid) {
      this.tfaForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const data = this.tfaForm.value as TfaVerifyRequest;

    if (this.data.isMfaEnabled) {
      this.submitDisableTfa(data);
    } else {
      this.submitEnableTfa(data);
    }
  }

  private submitEnableTfa(data: TfaVerifyRequest): void {
    this.userService.verifyTfaSetup(data).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        let errorMessage = "An error occured. Please try again";

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

  private submitDisableTfa(data: TfaVerifyRequest): void {
    this.userService.tfaDisable(data).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.dialogRef.close(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        let errorMessage = "An error occured. Please try again";

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

  onCancel(): void {
    this.dialogRef.close();
  }
}
