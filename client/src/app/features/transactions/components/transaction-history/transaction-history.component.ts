import { Component, Input, output } from '@angular/core';
import { CommonModule, DatePipe} from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { TransactionSummary } from '../../model/transaction-summary.model';
import { TransactionType } from '../../constants/transaction-type.enum';
import { Pagination } from '../../model/pagination.model';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDivider } from "@angular/material/divider";
import { AmountFormatPipe } from '../../../../shared/pipes/amount-format-pipe';
import { CategoryIconComponent } from "../../../../shared/components/category-icon/category-icon.component/category-icon.component";

@Component({
  selector: 'app-transaction-history',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    CommonModule,
    DatePipe,
    MatTooltipModule,
    MatDivider,
    AmountFormatPipe,
    CategoryIconComponent
],
  templateUrl: './transaction-history.component.html',
  styleUrl: './transaction-history.component.scss'
})
export class TransactionHistoryComponent {

  @Input() transactions: TransactionSummary[] = [];
  @Input() paginationInfo!: Pagination;

  // pagination outputs
  nextPageClick = output<void>();
  prevPageClick = output<void>();

  viewAttachmentClick = output<number>();

  editTransaction = output<number>();
  deleteTransaction = output<number>();

  onViewAttachment(transactionId: number, event: Event): void {
    event.stopPropagation();
    this.viewAttachmentClick.emit(transactionId);
  }

  isIncome(t: TransactionSummary): boolean {
    return t.type === TransactionType.INCOME;
  }

  isNext(): boolean {
    if (!this.paginationInfo) return false;
    return this.paginationInfo.hasNext;
  }

  isPrevious(): boolean {
    if (!this.paginationInfo) return false;
    return this.paginationInfo.page > 1;
  }

  calcFirstItemNumber(): number {
    if (!this.updatePaginationCheck()) return 0;

    if (this.paginationInfo.page === 1) {
      return 1;
    }
    return (this.paginationInfo.limit * (this.paginationInfo.page - 1) ) + 1;
  }

  calcLastItemNumber(): number {
    if (!this.updatePaginationCheck()) return 0;

    if (this.paginationInfo.page === 1 && !this.paginationInfo.hasNext) {
      return this.paginationInfo.size;
    }
    if (this.paginationInfo.page === this.paginationInfo.totalPages) {
      return this.paginationInfo.totalCount;
    }
    return this.paginationInfo.page * this.paginationInfo.limit;
  }

  onNextClick(): void {
    this.nextPageClick.emit();
  }

  onPrevClick(): void {
    this.prevPageClick.emit();
  }

  onEditClick(transactionId: number): void {
    this.editTransaction.emit(transactionId);
  }

  onDeleteClick(transactionId: number): void {
    this.deleteTransaction.emit(transactionId);
  }

  private updatePaginationCheck(): boolean {
    return !!this.paginationInfo;
  }
}
