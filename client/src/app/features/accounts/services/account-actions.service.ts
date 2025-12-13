import { inject, Injectable } from "@angular/core";
import { AccountDetails } from "../models/account.model";
import { Account } from "../../../core/models/account.model";
import { AccountService } from "../../../core/services/account.service";
import { MatDialog } from "@angular/material/dialog";
import { Router } from "@angular/router";
import { ConfirmDialog, ConfirmDialogData } from "../../../shared/components/dialogs/confirm-dialog/confirm-dialog";
import { filter, switchMap } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ApiErrorService } from "../../../core/services/api-error.service";
import { ToastService } from "../../../core/services/toast-service";

@Injectable(
  {
    providedIn: "root"
  }
)
export class AccountActionService {
  private accountService = inject(AccountService);
  private dialog = inject(MatDialog);
  private router = inject(Router);
  private errorService = inject(ApiErrorService);
  private toastService = inject(ToastService);

  deleteAccount(account: Account | AccountDetails, onSuccess?: () => void): void {
    if (account.status?.toUpperCase() === 'ACTIVE') {
      this.toastService.showError('You cannot delete an active account.');
      return;
    }

    const dialogData: ConfirmDialogData = {
      title: `Remove ${account.name}?`,
      message: 'Are you sure you want to delete your account? All data associated with this account will be deleted.',
      confirmButtonText: 'Yes, delete account',
      confirmButtonColor: 'warn'
    };

    this.dialog.open(ConfirmDialog, {
      data: dialogData,
      width: '450px',
      autoFocus: false
    }).afterClosed().pipe(
      filter(result => result === true),
      switchMap(() => this.accountService.deleteAccount(account.id))
    ).subscribe({
      next: () => {
        this.toastService.showSuccess('Account removed successfully.');

        if (onSuccess) {
          onSuccess();
        } else {
          this.router.navigate(['/app/accounts']);
        }
      },
      error: (err: HttpErrorResponse) => {
        this.errorService.handle(err);
      }
    });
  }
}