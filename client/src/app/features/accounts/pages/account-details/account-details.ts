import { Component, inject, signal } from '@angular/core';
import { AccountService } from '../../../../core/services/account.service';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin, of, switchMap } from 'rxjs';
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
import { CommonModule, CurrencyPipe, getCurrencySymbol } from '@angular/common';
import { AccountDetails } from '../../models/account.model';
import { AccountActionService } from '../../services/account-actions.service';
import { ApiErrorService } from '../../../../core/services/api-error.service';
import { TransactionService } from '../../../transactions/services/transaction.service';
import { TransactionSummary } from '../../../transactions/model/transaction-summary.model';
import { AnalyticsService } from '../../../../core/services/analytics.service';
import { CashFlowChartPoint, CategoryChartPoint, ChartPoint } from '../../../../core/models/analytics.model';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BalanceChartComponent } from '../../../../shared/components/charts/balance-chart.component';
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from '@angular/material/core';
import { CategorySumChartComponent } from "../../../../shared/components/charts/category-sum-chart.component";
import { CashFlowChartComponent } from "../../../../shared/components/charts/cash-flow-chart.component";

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
    MatSelectModule,
    BalanceChartComponent,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    CategorySumChartComponent,
    CashFlowChartComponent
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
  private transactionService = inject(TransactionService);
  private analyticsService = inject(AnalyticsService);

  account = signal<AccountDetails | null>(null);
  lastTransactions = signal<TransactionSummary[] | null>(null)
  isLoading = signal(true);

  balanceChartData = signal<ChartPoint[]>([]);
  categoryChartData = signal<CategoryChartPoint[]>([]);
  cashFlowChartData = signal<CashFlowChartPoint[]>([]);

  categoryType = signal<'EXPENSE' | 'INCOME'>('EXPENSE');
  activeRange = signal<string>('week');
  isChartLoading = signal(false);

  dateRange = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

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

        const accountRequest$ = this.accountService.getAccount(accountId);

        const transactionsRequest$ = this.transactionService.getTransactions({
          accountIds: [accountId],
          size: 10
        });

        return forkJoin({
          account: accountRequest$,
          transactions: transactionsRequest$
        })
      })
    ).subscribe({
      next: (result) => {
        if (!result) return;

        this.account.set(result.account);
        this.lastTransactions.set(result.transactions.data);
        this.initDefaultDateRange();
        this.loadAllCharts();
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
    this.router.navigate(['/app/accounts/edit', id], {
      state:
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

  private initDefaultDateRange(): void {
    this.setRange('month');
  }

  onDateChange(): void {
    if (this.dateRange.value.start && this.dateRange.value.end) {
      this.activeRange.set('');
      this.loadAllCharts();
    }
  }

  setRange(range: 'week' | 'month' | 'year'): void {
    const now = new Date();
    let start = new Date();
    const end = new Date();

    if (range === 'year') {
      start.setMonth(0, 1);
      this.activeRange.set('year');
    } else if (range === 'week') {
      const day = now.getDay() || 7;
      if (day !== 1) start.setHours(-24 * (day - 1));
      this.activeRange.set('week');
    } else if (range === 'month') {
      start.setDate(1);
      this.activeRange.set('month');
    }

    this.dateRange.patchValue({ start, end });
    this.loadAllCharts();
  }

  setCategoryType(type: 'EXPENSE' | 'INCOME'): void {
    this.categoryType.set(type);
    this.loadAllCharts();
  }

  loadAllCharts(): void {
    const acc = this.account();
    const { start, end } = this.dateRange.value;

    if (!acc || !start || !end) return;

    this.isChartLoading.set(true);

    forkJoin({
      balance: this.analyticsService.getAccountBalanceHistory(acc.id, start, end),
      category: this.analyticsService.getAccountCategoryBreakdown(acc.id, start, end, this.categoryType()),
      cashFlow: this.analyticsService.getAccountCashFlow(acc.id, start, end)
    }).subscribe({
      next: (results) => {
        this.balanceChartData.set(results.balance);
        this.categoryChartData.set(results.category);
        this.cashFlowChartData.set(results.cashFlow);
        this.isChartLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isChartLoading.set(false);
        this.errorService.handle(err);
      }
    });
  }
}
