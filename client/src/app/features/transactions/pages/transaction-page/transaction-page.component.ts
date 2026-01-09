import { Component, inject, OnInit, signal } from '@angular/core';
import { AccountSummary } from '../../model/account-summary.model';
import { CategorySummary } from '../../model/category-summary.model';
import { concatMap, filter, forkJoin, map, of, tap } from 'rxjs';
import { CategoryMapper } from '../../mappers/category.mapper';
import { AccountMapper } from '../../mappers/account.mapper';
import { MatDialog } from '@angular/material/dialog';
import { AddTransactionDialogComponent } from '../../components/add-transaction-dialog/add-transaction-dialog.component';
import { TransactionMapper } from '../../mappers/transaction.mapper';
import { TransactionService } from '../../../../core/services/transaction.service';
import { TransactionSummary } from '../../model/transaction-summary.model';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { TransactionTypeFilter } from '../../constants/transaction-type-filter.enum';
import { TransactionModeFilter } from '../../constants/transaction-mode-filter.enum';
import { TransactionDateRangeFilter } from '../../constants/transaction-data-range-filter.enum';
import { TransactionSortingDirection } from '../../constants/transaction-sorting-direction.enum';
import { TransactionSortByFilter } from '../../constants/transaction-sort-by-filter.enum';
import { TransactionFilterParams } from '../../model/transaction-filter-params.model';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { Pagination } from '../../model/pagination.model';
import { ConfirmDialog } from '../../../../shared/components/dialogs/confirm-dialog/confirm-dialog';
import { MatSnackBar} from '@angular/material/snack-bar';
import { UpdateTransactionDialogComponent } from '../../components/update-transaction-dialog/update-transaction-dialog.component';
import { TransactionUpdateRequest } from '../../model/transaction-update-request.model';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DateFilterComponent, DateRange } from "../../components/date-filter/date-filter.component";
import { DateFilterPresets } from '../../constants/date-filter-presets.enum';
import { TransactionHistoryComponent } from "../../components/transaction-history/transaction-history.component";
import { UpcomingTransactionsComponent } from '../../components/upcoming-transactions/upcoming-transactions.component';
import { UpcomingTransactionSummary } from '../../model/upcoming-transaction-summary.model';
import { UpcomingTransactionsFilterParams } from '../../model/upcoming-transaction-filter-params.mode';
import { UpcomingTransactionsTimeRange } from '../../constants/upcoming-transactions-time-range.enum';
import { CategoryType } from '../../../../core/models/category-response-dto.model';
import { TransactionCategoryChangeRequest } from '../../model/transaction-category-change-request.model';
import { RouterLink } from '@angular/router';
import { AccountService } from '../../../../core/services/account.service';
import { CategoryService } from '../../../../core/services/category.service';
import { TransactionAttachmentManager } from '../../services/transaction-attachment-manager.service';
import { AttachmentViewDialog } from '../../components/attachment-view-dialog/attachment-view-dialog.component';

@Component({
  selector: 'app-transaction-page',
  standalone: true,
  imports: [
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatDividerModule,
    MatListModule,
    MatMenuModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    CommonModule,
    FormsModule,
    DateFilterComponent,
    TransactionHistoryComponent,
    UpcomingTransactionsComponent,
    RouterLink
],
  providers: [DatePipe],
  templateUrl: './transaction-page.component.html',
  styleUrl: './transaction-page.component.scss'
})
export class TransactionPageComponent implements OnInit{

  selectedType = signal<TransactionTypeFilter>(TransactionTypeFilter.ALL);
  selectedPeriod = signal<string>(DateFilterPresets.LAST_30_DAYS);

  pagination!: Pagination;
  upcomingPagination!: Pagination;

  accounts: AccountSummary[] = [];
  categories: CategorySummary[] = [];
  transactions: TransactionSummary[] = [];
  upcomingTransactions: UpcomingTransactionSummary[] = [];

  readonly dialog = inject(MatDialog);
  private accountService = inject(AccountService);
  private categoryService = inject(CategoryService);
  private transactionService = inject(TransactionService);
  private readonly datePipe = inject(DatePipe);
  private snackBar = inject(MatSnackBar);
  private attachmentManager = inject(TransactionAttachmentManager);

  modeOptions = [
    { value: TransactionModeFilter.ALL, label: 'All' },
    { value: TransactionModeFilter.REGULAR, label: 'Regular' },
    { value: TransactionModeFilter.RECURRING, label: 'Recurring' }
  ];

  sortByOptions = [
    { value: TransactionSortByFilter.DATE, label: 'Date'},
    { value: TransactionSortByFilter.AMOUNT, label: 'Amount' },
    { value: TransactionSortByFilter.CATEGORY, label: 'Category' }
  ];

  transactionsfilter: TransactionFilterParams = {
    type: TransactionTypeFilter.ALL,
    mode: TransactionModeFilter.ALL,
    accountIds: undefined,
    categoryIds: undefined,
    since: undefined,
    to: undefined,
    page: 1,
    limit: 5,
    sortedBy: TransactionSortByFilter.DATE,
    sortDirection: TransactionSortingDirection.DESC
  };

  upcomingTransactionsFilter: UpcomingTransactionsFilterParams = {
    page: 1,
    limit: 3,
    range: UpcomingTransactionsTimeRange.NEXT_7_DAYS,
    accountIds: []
  }

  selectedMode = TransactionModeFilter.ALL;
  selectedAccount = 'ALL';
  selectedCategory = 'ALL';
  selectedSortingDirection = TransactionSortingDirection.DESC;
  selectedSortBy = TransactionSortByFilter.DATE;

  ngOnInit(): void {
    this.loadAccounts();
    this.loadCategories();
    this.loadTransactions();
    this.loadUpcomingTransactions();
  }

  handleAddTransactionEvent(): void {
    this.openAddTransactionDialog();
  }

  onTypeChange(event: MatSelectChange): void {
    console.log("Type changed: ", event.value);
    this.updateFilter({type: event.value});
  }

  onModeChange(event: MatSelectChange): void {
    console.log("Mode change: ", event.value);
    this.updateFilter({mode: event.value});
  }

  onCategoryChange(event: MatSelectChange): void {
    console.log("Category change: ", event.value);
    const value = event.value;

    const categoryIds = value === 'ALL' ? undefined : [value];
    this.updateFilter({categoryIds});
  }

  onAccountChange(event: MatSelectChange): void {
    console.log("Account change: ", event.value);

    const value = event.value;
    const accountIds = value === 'ALL' ? undefined : [value];
    this.updateFilter({accountIds});
  }

  onSortByChange(event: MatSelectChange): void {
    console.log("Sort by field changed: ", event.value);

    this.updateFilter({sortedBy: event.value});
  }

  onSortDirectionChange(): void {
    console.log("Sort direction changed");

    const newDirection = this.selectedSortingDirection === TransactionSortingDirection.DESC
      ? TransactionSortingDirection.DESC
      : TransactionSortingDirection.ASC;

      console.log("New direction: ", newDirection);

    this.selectedSortingDirection = newDirection;
    this.updateFilter({sortDirection: newDirection});
  }

  setType(type: string) {
    let newValue;
    switch (type) {
      case TransactionTypeFilter.EXPENSE:
        newValue = TransactionTypeFilter.EXPENSE;
        break;
      case TransactionTypeFilter.INCOME:
        newValue = TransactionTypeFilter.INCOME;
        break;
      default:
        newValue = TransactionTypeFilter.ALL;
    }
    console.log("Type changed: ", newValue);
    this.selectedType.set(newValue);
    this.updateFilter({type: newValue});
  }

  onDateSelect(range: DateRange) {
    console.log("Selected range: ", range);

    this.selectedPeriod.set(this.formatDateRangeToString(range));

    if (range.start !== null && range.end !== null) {
      const start = this.formatDate(range.start);
      const end = this.formatDate(range.end);
      this.updateFilter({since: start, to: end});
    }
  }

  onUpcomingRangeChange(range: UpcomingTransactionsTimeRange): void {
    console.log("upcoming date range change: ", range);
    this.updateUpcomingFilter({range});
  }

  formatDateRangeToString(range: DateRange): string {
    const start = this.datePipe.transform(range.start, 'dd.MM.yyyy');
    const end = this.datePipe.transform(range.end, 'dd.MM.yyyy');
    return `${start} - ${end}`;
  }

  private updateUpcomingFilter(changes: Partial<UpcomingTransactionsFilterParams>): void {
    this.upcomingTransactionsFilter = {
      ...this.upcomingTransactionsFilter,
      ...changes,
      page: 1
    };

    console.log('Nowy stan filtra:', this.upcomingTransactionsFilter);
    this.loadUpcomingTransactions();
  }

  private updateFilter(changes: Partial<TransactionFilterParams>): void {
    this.transactionsfilter = {
      ...this.transactionsfilter,
      ...changes,
      page: 1
    };

    console.log('Nowy stan filtra:', this.transactionsfilter);
    this.loadTransactions();
  }

  private calculateDateRange(range: TransactionDateRangeFilter): void {

    let since: string | undefined;
    let to: string | undefined;

    switch (range) {

      case TransactionDateRangeFilter.LAST_WEEK:
        const now = new Date();
        const lastWeek = new Date();
        lastWeek.setDate(now.getDate() - 7);

        to = undefined;
        since = this.formatDate(lastWeek);

        break;

      case TransactionDateRangeFilter.LAST_MONTH:
        since = undefined;
        to = undefined;
        break;

      case TransactionDateRangeFilter.LAST_THREE_MONTHS:
        const now3 = new Date();
        const lastThreeMonths = new Date();
        lastThreeMonths.setMonth(now3.getMonth() - 3);

        to = undefined;
        since = this.formatDate(lastThreeMonths);
        break;

      case TransactionDateRangeFilter.LAST_SIX_MONTHS:
        const now6 = new Date();
        const lastSixMonths = new Date();
        lastSixMonths.setMonth(now6.getMonth() - 6);

        to = undefined;
        since = this.formatDate(lastSixMonths);
    }
    this.updateFilter({ since, to });
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  async onEditTransaction(transactionId: number) {

    const transactionToUpdate = this.transactions.find( (tr) => tr.id === transactionId);

    if (transactionToUpdate) {

      let attachmentData = null;
      if (transactionToUpdate.hasAttachment) {
        attachmentData = await this.attachmentManager.getTransactionAttachmentData(transactionId);
      }

      // getting categories with the same type as transaction type
      const catogiesToPass = this.categories.filter( (cat) => {
        return String(cat.type) === String(transactionToUpdate.type) || cat.type === CategoryType.GENERAL;
      });
      const currentCategoryId = transactionToUpdate.category.id;

      const dialogRef = this.dialog.open(UpdateTransactionDialogComponent, {
        width: '600px',
        maxHeight: '85vh',
        data: {
          title: transactionToUpdate.title,
          amount: transactionToUpdate.amount,
          description: transactionToUpdate.description,
          categories: catogiesToPass,
          categoryId: currentCategoryId, // current category
          isRecurring: transactionToUpdate.recurringTransactionId? true : false,
          attachmentData
        }
      });

      dialogRef.afterClosed().pipe(

        filter(result => !!result),

        concatMap((result) => {
          console.log("Gathered form data", result);
          const {transactionData, file} = result;

          const reqs$ = [];

          if (transactionData.categoryId !== currentCategoryId) {
            if (!transactionToUpdate.recurringTransactionId) {
              console.log("Category changed");
              const changeCategoryReq: TransactionCategoryChangeRequest = {
                currentCategoryId: currentCategoryId,
                newCategoryId: transactionData.categoryId,
                accountId: transactionToUpdate.account.id
              };
              reqs$.push(this.transactionService.changeTransactionCategory(transactionId, changeCategoryReq));
            }
          }
          
          if (transactionData.title !== transactionToUpdate.title ||
              transactionData.amount !== transactionToUpdate.amount ||
              transactionData.description !== transactionToUpdate.description
          ) {
            console.log("General data changed");

            const updateReq: TransactionUpdateRequest = {
              title: transactionData.title,
              amount: transactionData.amount,
              description: transactionData.description
            };
            reqs$.push(this.transactionService.updateTransaction(transactionId, updateReq));
          }
          
          if (file) {
            console.log("Updating attachment");
            reqs$.push(this.attachmentManager.manageAttachmentUpload(file, transactionId));
          }

          if (reqs$.length === 0) {
            console.log("Nothing changed");
            return of(null);
          }
          return forkJoin(reqs$).pipe(
            tap(() => this.loadTransactions())
          );
        }),
      ).subscribe({
        error: (err) => console.error("Error occurred when saving changes", err)
      });
    }
  }

  onDeleteTransaction(id: number) {
    console.log('Delete', id);

    const dialogRef = this.dialog.open(ConfirmDialog, {
      width: '400px',
      data: {
        title: 'Delete Transaction',
        message: 'Are you sure you want to delete this transaction? This action cannot be undone.',
        confirmButtonText: 'Delete',
        confirmButtonColor: 'warn'
      }
    });

    dialogRef.afterClosed().subscribe((result: boolean) => {

      if (result === true) {

        this.transactionService.deleteTransaction(id).subscribe({

          next: () => {
            this.loadTransactions();

            this.snackBar.open('Transaction deleted successfully', 'Close', {
              duration: 3000
            });

            console.log(`Transaction with id: ${id} deleted`);
          },

          error: (err) => {
            console.error('Error deleting transaction', err);
            this.snackBar.open('Failed to delete transaction', 'Close', {
              duration: 3000,
              panelClass: ['error-snackbar']
            });
          }
        });
      }
    });
  }

  prevPage() {
    if (!this.pagination) return;
    if (this.pagination.page > 1) {
      this.transactionsfilter = {
        ...this.transactionsfilter,
        page: this.pagination.page - 1,
      };
      this.loadTransactions();
    }
  }

  nextPage() {
    if (!this.pagination) return;
    if (this.pagination.page < this.pagination.totalPages) {
      this.transactionsfilter = {
        ...this.transactionsfilter,
        page: this.pagination.page + 1,
      };
      this.loadTransactions();
    }
  }

  upcomingNextPage(): void {
    if (!this.upcomingPagination) return;
    if (this.upcomingPagination.page < this.upcomingPagination.totalPages) {
      this.upcomingTransactionsFilter = {
        ...this.upcomingTransactionsFilter,
        page: this.upcomingPagination.page + 1,
      };
      this.loadUpcomingTransactions();
    }
  }

  upcomingPrevPage(): void {
    if (!this.upcomingPagination) return;
    if (this.upcomingPagination.page > 1) {
      this.upcomingTransactionsFilter = {
        ...this.upcomingTransactionsFilter,
        page: this.upcomingPagination.page - 1,
      };
      this.loadUpcomingTransactions();
    }
  }

  openAddTransactionDialog(): void {
      const dialogRef = this.dialog.open(AddTransactionDialogComponent, {
        data: {
          accountList: this.accounts,
          categoryList: this.categories
        },
        width: '600px',
        maxHeight: '90vh'
      });

      dialogRef.afterClosed().pipe(

        filter(result => !!result),

        concatMap((result) => {
          const {transactionData, file} = result;
          console.log("Gathered form data", result);
          
          const dto = TransactionMapper.toCreateRequest(transactionData);

          return this.transactionService.createTransaction(dto).pipe(

            concatMap((createdTransactionData) => {
              console.log("Transaction created: ", createdTransactionData);

              if (file) {
                return this.attachmentManager.manageAttachmentUpload(file, createdTransactionData.id);
              } else {
                return of(null);
              }
            })
          );
        }),

        tap(() => {
          console.log("Transaction added successfully");
          this.loadTransactions();
        })
      ).subscribe({
        error: (err) => {
          console.error("Error occurred when adding transaction", err);
        }
      });
  }

  async openAttachmentViewDialog(transactionId: number): Promise<void> {

    console.log("Opening attachment view dialog for transaction with id: ", transactionId);

    try {
      const data = await this.attachmentManager.getTransactionAttachmentData(transactionId);
    
      if (data) {
        this.dialog.open(AttachmentViewDialog, {
          width: '600px',
          data: data
        });
      } else {
        console.error("Error occurred when fetching attachment data");
      }

    } catch (error) {
      console.error("Unexpected error occurred", error);
    }

  }

  loadTransactions(): void {
    this.transactionService.getTransactions(this.transactionsfilter).subscribe({
      next: (response) => {
        console.log("fetching transactions first page: ", response);

        this.transactions = response.data;

        this.pagination = response.pagination;
      },
      error: (error) => {
        console.log("error occurred when fetching transactions", error);
      }
    })
  }

  loadAccounts(): void {
    this.accountService.getAccounts().pipe(
      map(dtos => dtos.map(AccountMapper.toSummary))
    )
    .subscribe({
      next: (data) => {
        console.log("fetching accounts", data);
        this.accounts = data;
      },
      error: (error) => {
        console.log("error occured when fetching accounts", error);
      }
    });
  }

  loadUpcomingTransactions(): void {
    this.transactionService.getUpcomingTransactions(this.upcomingTransactionsFilter)
    .subscribe({
      next: (response) => {
        this.upcomingTransactions = response.data;

        this.upcomingPagination = response.pagination;
        console.log("upcoming transactions fetched: ", response);
      },
      error: (error) => {
        console.log("error occurred when fetching upcoming transactions", error);
      }
    })
  }

  loadCategories(): void {
    this.categoryService.getCategories().pipe(
      map (dtos => dtos.map(CategoryMapper.toSummary))
    )
    .subscribe({
      next: (data) => {
        console.log('fetching categories', data);
        this.categories = data;
      },
      error: (error) => {
        console.log("error occurred when fetching categories", error);
    }
    });
  }
}
