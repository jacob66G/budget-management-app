import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../../../core/services/auth.service';
import { UpdateUserRequest } from '../../model/user-profile.model';
import { UserService } from '../../../../../core/services/user.service';
import { User } from '../../../../../core/models/user.model';
import { HttpErrorResponse } from '@angular/common/http';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-profile-details',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
],
  templateUrl: './profile-details.html',
  styleUrl: './profile-details.scss'
})
export class ProfileDetails {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private snackBar = inject(MatSnackBar);

  isLoading = signal(false);
  profileForm!: FormGroup;
  currentUser = this.authService.currentUser;

  ngOnInit(): void {
    this.profileForm = this.fb.group(
      {
        name: ["this.currentUser()?.name", [Validators.required, Validators.minLength(2), Validators.maxLength(30)]],
        surname: ["this.currentUser()?.surname", [Validators.required, Validators.minLength(2), Validators.maxLength(30)]],
      }
    )
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }
    this.isLoading.set(true)
    const dto = this.profileForm.value as UpdateUserRequest

    this.userService.updateUser(dto).subscribe({
      next: (response: User) => {
          this.isLoading.set(false);
          this.authService.currentUser.set(response);
          this.snackBar.open('Data updated successfully', 'Close', { duration: 3000 });
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading.set(false);
        let errorMessage = 'An error occurred. Please try again.';
        if (error.error && error.error.message) {
          errorMessage = error.error.message;
        }
        this.snackBar.open(errorMessage, 'OK', { duration: 5000 });
        this.profileForm.patchValue({
          name: this.currentUser()?.name,
          surname: this.currentUser()?.surname,
        });
      }
    })
  }
}
