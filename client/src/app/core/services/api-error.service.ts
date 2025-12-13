import { HttpErrorResponse } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { ToastService } from "./toast-service";

@Injectable({
  providedIn: 'root'
})
export class ApiErrorService {
 private toastService = inject(ToastService);

  handle(err: HttpErrorResponse, defaultMessage: string = 'An error occurred. Please try again.'): void {
    let errorMessage = defaultMessage;

    if (err.error) {
      if (err.error.message) {
        errorMessage = err.error.message;
      }

      if (err.error.fieldErrors) {
        const messages = Object.values(err.error.fieldErrors);
        if (messages.length > 0) {
          errorMessage = messages.join('\n');
        }
      }
    }

    this.toastService.showError(errorMessage);
  }
}