import { HttpErrorResponse } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";

@Injectable({
  providedIn: 'root'
})
export class ApiErrorService {
  private snackBar = inject(MatSnackBar);

  handle(err: HttpErrorResponse, defaultMessage: string = 'An error occurred. Please try again.'): void {
    let errorMessage = defaultMessage;

    if (err.error) {
      if (err.error.message) {
        errorMessage = err.error.message;
      }

      if (err.error.fieldErrors) {
        const fields = Object.entries(err.error.fieldErrors)
          .map(([key, value]) => `${key}: ${value}`)
          .join(', ');
        
        if (fields) {
            errorMessage = fields;
        }
      }
    }

    this.showError(errorMessage);
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'OK', {
      duration: 5000,
      panelClass: 'error-snackbar'
    });
  }
}