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

@Component({
  selector: 'app-transaction-page',
  imports: [HeaderComponent],
  templateUrl: './transaction-page.component.html',
  styleUrl: './transaction-page.component.css'
})
export class TransactionPageComponent implements OnInit{

  accounts: AccountSummary[] = [{id:1, name: 'main', currency: 'pln'}, {id:3, name: 'saving', currency: 'pln'}, {id:3, name: 'credit', currency: 'pln'}];
  categories: CategorySummary[] = [{id:1, name: 'groceries', type: CategoryType.EXPENSE, iconKey: 'null'},
    {id:2, name: 'tv', type: CategoryType.INCOME, iconKey: 'null'},
    {id:3, name: 'utilities', type: CategoryType.GENERAL, iconKey: 'null'}];
  transactionTypes = Object.values(TransactionType);

  headerTitle: string = 'Transactions';
  createTransactionButtonTitle: string = 'Add Transaction';

  readonly dialog = inject(MatDialog);

  constructor(
    private accountService: AccountService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.loadAccounts();
    this.loadCategories();
  }

  handleAddTransactionEvent(): void {
    this.openAddTransactionDialog();
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
