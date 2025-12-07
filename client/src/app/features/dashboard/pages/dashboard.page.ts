import { Component, inject, OnInit, signal } from '@angular/core';

import { MatIconModule } from '@angular/material/icon';
import { CommonModule, CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { AnalyticsService } from '../../../core/services/analytics.service';
import { FinancialSummaryResponse } from '../models/financial-summary.model';
import { CashFlowChartComponent } from "../../../shared/components/charts/cash-flow-chart.component";
import { BalanceChartComponent } from "../../../shared/components/charts/balance-chart.component";
import { CategorySumChartComponent } from "../../../shared/components/charts/category-sum-chart.component";
import { CashFlowChartPoint, CategoryChartPoint, MultiSeriesChart } from '../../../core/models/analytics.model';
import { ApiErrorService } from '../../../core/services/api-error.service';
import { HttpErrorResponse } from '@angular/common/http';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule, MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, MatDividerModule, MatTooltipModule,
    MatDatepickerModule, MatFormFieldModule, MatNativeDateModule,
    DatePipe, DecimalPipe,
    CashFlowChartComponent,
    BalanceChartComponent,
    CategorySumChartComponent
],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss'
})
export class DashboardPage implements OnInit {
  private analyticsService = inject(AnalyticsService);
  private errorService = inject(ApiErrorService);
  private router = inject(Router);

  summary = signal<FinancialSummaryResponse | null>(null);
  isLoading = signal(true);

  balanceChartData = signal<MultiSeriesChart | null>(null);
  categoryChartData = signal<CategoryChartPoint[]>([]);
  cashFlowChartData = signal<CashFlowChartPoint[]>([]);

  isBalanceLoading =  signal(true);
  isCategoryLoading =  signal(true);
  isCashFlowLoading = signal(true);
  activeRange = signal<string>('month');

  dateRangeForm = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null)
  });

  ngOnInit(): void {
    this.setRange('month');
  }

  loadData(): void {
    const { start, end } = this.dateRangeForm.value;
    if (!start || !end) return;

    this.isLoading.set(true);

    this.analyticsService.getGlobalFinancial(start, end).subscribe({
      next: (data) => {
        this.summary.set(data);
        this.isLoading.set(false);

        this.loadChartsData();
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err);
      }
    });
  }

  loadChartsData(): void {
    const { start, end } = this.dateRangeForm.value;
    if (!start || !end) return;

    this.isBalanceLoading.set(true);
    this.isCategoryLoading.set(true);
    this.isCashFlowLoading.set(true);

    this.analyticsService.getGlobalBalanceHistory(start, end).subscribe({
      next: (res) => {
        this.balanceChartData.set(res);
        this.isBalanceLoading.set(false);
      },
      error: () => this.isBalanceLoading.set(false)
    });

    this.analyticsService.getGlobalCategoryBreakdown(start, end, 'EXPENSE').subscribe({
      next: (res) => {
        this.categoryChartData.set(res);
        this.isCategoryLoading.set(false);
      },
      error: () => this.isCategoryLoading.set(false)
    });

    this.analyticsService.getGlobalCashFlow(start, end).subscribe({
      next: (res) => {
        this.cashFlowChartData.set(res);
        this.isCashFlowLoading.set(false);
      },
      error: () => this.isCashFlowLoading.set(false)
    });
  }

  onDateChange(): void {
    if (this.dateRangeForm.value.start && this.dateRangeForm.value.end) {
      this.activeRange.set('custom');
      this.loadData();
    }
  }

  setRange(range: 'month' | '3months' | '6months' | 'year'): void {
    this.activeRange.set(range);
    const end = new Date();
    const start = new Date();

    if (range === 'month') {
      start.setDate(1);
    } else if (range === '3months') {
      start.setMonth(start.getMonth() - 3);
    } else if (range === '6months') {
      start.setMonth(start.getMonth() - 6);
    } else if (range === 'year') {
      start.setFullYear(start.getFullYear() - 1);
    }

    this.dateRangeForm.setValue({ start, end });
    this.loadData();
  }

  onViewTransactions(): void {
      this.router.navigate(['/app/transactions']);
  }

  getTypeClass(type: string): string {
    switch (type) {
      case 'INCOME': return 'type-income';
      case 'EXPENSE': return 'type-expense';
      case 'GENERAL': return 'type-general';
      default: return '';
    }
  }
}
