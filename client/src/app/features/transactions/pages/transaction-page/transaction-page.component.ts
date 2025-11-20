import { Component, inject, OnInit } from '@angular/core';
import { AccountService } from '../../../accounts/services/account.service';
import { CategoryService } from '../../../categories/services/category.service';
import { AccountSummary } from '../../model/account-summary.model';
import { CategorySummary } from '../../model/category-summary.model';
import { map } from 'rxjs';
import { CategoryMapper } from '../../mappers/category.mapper';
import { AccountMapper } from '../../mappers/account.mapper';
import { HeaderComponent } from '../../components/header/header.component';
import { MatDialog } from '@angular/material/dialog';
import { AddTransactionDialogComponent } from '../../components/add-transaction-dialog/add-transaction-dialog.component';
import { TransactionType } from '../../constants/transaction-type.enum';
import { CategoryType } from '../../../../core/models/category-response-dto.model';
import { TransactionCreateRequest } from '../../model/transaction-create-request.model';
import { TransactionMapper } from '../../mappers/transaction.mapper';
import { TransactionService } from '../../services/transaction.service';
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
import { UpperCasePipe } from '@angular/common';

@Component({
  selector: 'app-transaction-page',
  imports: [HeaderComponent, MatCardModule, MatFormFieldModule, MatSelectModule, MatIconModule, UpperCasePipe],
  templateUrl: './transaction-page.component.html',
  styleUrl: './transaction-page.component.scss'
})
export class TransactionPageComponent implements OnInit{

  accounts: AccountSummary[] = [];
  categories: CategorySummary[] = [];
  transactions: TransactionSummary[] = [];

  transactionTypes = Object.values(TransactionType);
  headerTitle: string = 'Transactions';
  createTransactionButtonTitle: string = 'Add Transaction';
  sortDirection = 'desc';

  readonly dialog = inject(MatDialog);
  private accountService = inject(AccountService);
  private categoryService = inject(CategoryService);
  private transactionService = inject(TransactionService);

  typeOptions = [
    { value: TransactionTypeFilter.ALL, label: 'All' },
    { value: TransactionTypeFilter.INCOME, label: 'Income' },
    { value: TransactionTypeFilter.EXPENSE, label: 'Expense' }
  ];
  selectedType = TransactionTypeFilter.ALL;
  
  modeOptions = [
    { value: TransactionModeFilter.ALL, label: 'All' },
    { value: TransactionModeFilter.REGULAR, label: 'Regular' },
    { value: TransactionModeFilter.RECURRING, label: 'Recurring' }
  ];
  selectedMode = TransactionModeFilter.ALL;

  dateRangeOptions = [
    { value: TransactionDateRangeFilter.LAST_MONTH, label: 'Last month'},
    { value: TransactionDateRangeFilter.LAST_THREE_MONTHS, label: 'Last 3 months' },
    { value: TransactionDateRangeFilter.CUSTOM_DATE_RANGE, label: 'Custom range' }
  ];
  selectedDateRange = TransactionDateRangeFilter.LAST_MONTH;

  selectedAccount = 'ALL';
  selectedCategory = 'ALL';

  sortByOptions = [
    { value: TransactionSortByFilter.DATE, label: 'Date'},
    { value: TransactionSortByFilter.AMOUNT, label: 'Amount' },
    { value: TransactionSortByFilter.CATEGORY, label: 'Category' }
  ];
  selectedSortBy = TransactionSortByFilter.DATE;

  selectedSortingDirection = TransactionSortingDirection.DESC;

  ngOnInit(): void {
    this.loadAccounts();
    this.loadCategories();
    this.loadTransactions();
  }

  handleAddTransactionEvent(): void {
    this.openAddTransactionDialog();
  }

  onTypeChange(event: MatSelectChange): void {
    console.log("Type changed: ", event.value);
  }
  
  onModeChange(event: MatSelectChange): void {
    console.log("Mode change: ", event.value);
  }

  onCategoryChange(event: MatSelectChange): void {
    console.log("Category change: ", event.value);
  }

  onAccountChange(event: MatSelectChange): void {
    console.log("Account change: ", event.value);
  }

  onDateRangeChange(event: MatSelectChange): void {
   console.log("Date range change: ", event.value); 
  }

  onSortDirectionChange(event: any): void {
    console.log("Sort direction changed");
  }

  openAddTransactionDialog(): void {
      const dialogRef = this.dialog.open(AddTransactionDialogComponent, {
        data: {
          accountList: this.accounts,
          categoryList: this.categories,
          transactionTypes: this.transactionTypes
        },
        width: '700px',
        height: '700px'
      });

      dialogRef.afterClosed().subscribe( formData => {

        console.log("closing dialog")

        if (formData) {

          console.log("Gathering form data after closing dialog");
          console.log("Form Data: ", formData);

          const dto: TransactionCreateRequest = TransactionMapper.toCreateRequest(formData);
          console.log("TransactionCreateRequest: ", dto);

          this.transactionService.createTransaction(dto).subscribe({
            next: (data) => {
              console.log("transaction created: ", data);
              const category = this.categories.find( (cat) => cat.id === dto.categoryId);
              const account = this.accounts.find( (acc) => acc.id === dto.accountId);
              if (category && account) {

                const { accountId, categoryId, ...baseData } = dto;

                const transaction: TransactionSummary = {
                  ...baseData,
                  id: data.id,
                  transactionDate: data.transactionDate,
                  category: category,
                  account: account,
                  recurringTransactionsId: 0,

                }
              this.transactions.push(transaction);
              }
              
            },
            error: (error) => {
              console.log("error occurred when creating transaction");
            }
          })

          console.log("transactions list: ");
          console.log(this.transactions);
        }
        else {
          console.log("Dialog close without any data");
        }
      });
  }

  loadTransactions(): void {
    this.transactionService.getTransactions().subscribe({
      next: (data) => {
        console.log("fetching transactions first page: ", data);
        this.transactions = data;
      },
      error: (error) => {
        console.log("error occurred when fetching transactions");
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
