import { Component, inject, signal } from '@angular/core';
import { AccountService } from '../../../../core/services/account.service';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, switchMap } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { AccountDetails } from '../../models/account.model';
import { AccountActionService } from '../../services/account-actions.service';
import { ApiErrorService } from '../../../../core/services/api-error.service';

@Component({
  selector: 'app-account-details',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    CurrencyPipe,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatDialogModule,
    MatTooltipModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './account-details.html',
  styleUrl: './account-details.scss'
})
export class AccountDetailsPage {
  private accountService = inject(AccountService);
  private accountAction = inject(AccountActionService)
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  private errorService = inject(ApiErrorService)

  account = signal<AccountDetails | null>(null);
  isLoading = signal(true);

  ngOnInit(): void {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        const idString = params.get('id');

        if (!idString) {
          this.isLoading.set(false);
          this.router.navigate(['/app/accounts']);
          return of(null);
        }

        const accountId = Number(idString);

        return this.accountService.getAccount(accountId);
      })
    ).subscribe({
      next: (accountData: AccountDetails | null) => {
        this.account.set(accountData);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.account.set(null);
        this.errorService.handle(err);
      }
    });
  }

  onEditClick(id: number): void {
    this.router.navigate(['/app/accounts/edit', id], {state: 
      {
        accountData: this.account(),
        returnUrl: this.router.url
      }
    });
  }

  onDeleteClick(account: AccountDetails): void {
    this.accountAction.deleteAccount(account);
  }

  onStatusToggle(account: AccountDetails): void {
    if (this.isLoading()) return;

    const isActive = account.status.toUpperCase() === 'ACTIVE';

    const request$ = isActive
      ? this.accountService.deactivateAccount(account.id)
      : this.accountService.activateAccount(account.id);

    this.isLoading.set(true);

    request$.subscribe({
      next: (updatedAccount) => {
        this.account.set(updatedAccount);
        this.isLoading.set(false);

        const message = isActive ? 'Your account has been deactivated.' : 'Your account has been activated.';
        this.snackBar.open(message, 'OK', { duration: 3000, panelClass: 'success-snackbar' });
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err, 'The status could not be changed. Please try again.');
      }
    });
  }

  onViewTransactions(accountId: number): void {
    this.router.navigate(['/app/transactions'], { queryParams: { accountId: accountId } });
  }

  getToggleStatusText(status: string): string {
    return status === 'ACTIVE' ? 'Deactivate' : 'Activate';
  }

}
