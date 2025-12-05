import { Component, input, Input, output, signal } from '@angular/core';
import { TransactionType } from '../../constants/transaction-type.enum';
import { UpcomingTransactionSummary } from '../../model/upcoming-transaction-summary.model';
import { MatIcon } from "@angular/material/icon";
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { UpcomingTransactionsTimeRange } from '../../constants/upcoming-transactions-time-range.enum';
import { Pagination } from '../../model/pagination.model';
import { MatIconModule } from '@angular/material/icon';


@Component({
  selector: 'app-upcoming-transactions',
  imports: [
    MatIcon,
    CommonModule,
    DatePipe,
    CurrencyPipe,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './upcoming-transactions.component.html',
  styleUrl: './upcoming-transactions.component.scss'
})
export class UpcomingTransactionsComponent {

  protected readonly TimeRange = UpcomingTransactionsTimeRange;

  // Stan wybranego filtra (domyślnie 7 dni)
  selectedRange = signal<UpcomingTransactionsTimeRange>(UpcomingTransactionsTimeRange.NEXT_7_DAYS);

  dateRangeChange = output<UpcomingTransactionsTimeRange>();
  nextPageClick = output<void>();
  prevPageClick = output<void>();

  @Input() upcomingTransactions: UpcomingTransactionSummary[] = [];
  @Input() paginationInfo!: Pagination;

  setRange(range: UpcomingTransactionsTimeRange) {
    if (this.selectedRange() === range) {
      return;
    }

    this.selectedRange.set(range);
    this.dateRangeChange.emit(range);
  }

  onDateRangeChange(): void {
    this.dateRangeChange.emit(this.selectedRange())
  }

  isIncome(t: UpcomingTransactionSummary): boolean {
    return t.type === TransactionType.INCOME;
  }

  isNext(): boolean {
    if (this.paginationInfo.hasNext) {
      return true;
    }
    return false;
  }

  isPrevious(): boolean {
    if (this.paginationInfo.page > 1) {
      return true;
    }
    return false;
  }

  calcFirstItemNumber(): number {
    if (this.paginationInfo.page === 1) {
      return 1;
    }
    return (this.paginationInfo.limit * (this.paginationInfo.page - 1) ) + 1;
  }

  calcLastItemNumber(): number {
    if (this.paginationInfo.page === 1 && !this.paginationInfo.hasNext) {
      return this.paginationInfo.size;
    }
    return this.paginationInfo.page * this.paginationInfo.limit;
  }

  onNextClick(): void {
    this.nextPageClick.emit();
  }

  onPrevClick(): void {
    this.prevPageClick.emit();
  }
}
