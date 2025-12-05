import { Component, Input, output } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { TransactionSummary } from '../../model/transaction-summary.model';
import { TransactionType } from '../../constants/transaction-type.enum';
import { Pagination } from '../../model/pagination.model';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
  selector: 'app-transaction-history',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    CommonModule,
    DatePipe,
    CurrencyPipe,
    MatTooltipModule
  ],
  templateUrl: './transaction-history.component.html',
  styleUrl: './transaction-history.component.scss'
})
export class TransactionHistoryComponent {

  @Input() transactions: TransactionSummary[] = [];
  @Input() paginationInfo!: Pagination;

  nextPageClick = output<void>();
  prevPageClick = output<void>();

  isIncome(t: TransactionSummary): boolean {
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
