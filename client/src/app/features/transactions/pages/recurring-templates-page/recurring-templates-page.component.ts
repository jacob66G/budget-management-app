import { Component, inject, OnInit, signal } from '@angular/core';
import { TransactionType } from '../../constants/transaction-type.enum';
import { RecurringInterval } from '../../constants/recurring-interval.enum';
import { RecurringTemplateSummary } from '../../model/recurring-template-summary.model';
import { MatIconModule } from "@angular/material/icon";
import { MatMenuModule } from "@angular/material/menu";
import { MatButtonModule } from '@angular/material/button';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AmountFormatPipe } from '../../../../shared/pipes/amount-format-pipe';
import { TransactionService } from '../../services/transaction.service';
import { Pagination } from '../../model/pagination.model';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDivider } from "@angular/material/divider";
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { AddRecurringTemplateDialogComponent } from '../../components/add-recurring-template-dialog/add-recurring-template-dialog.component';
import { CategorySummary } from '../../model/category-summary.model';
import { AccountSummary } from '../../model/account-summary.model';
import { CategoryMapper } from '../../mappers/category.mapper';
import { map } from 'rxjs';
import { AccountMapper } from '../../mappers/account.mapper';
import { RecurringTransactionCreateRequest } from '../../model/recurring-template-create-request.model';
import { RecurringTemplateMapper } from '../../mappers/recurring-template.mapper';
import { AccountService } from '../../../../core/services/account.service';
import { CategoryService } from '../../../../core/services/category.service';

@Component({
  selector: 'app-recurring-templates-page',
  imports: [
    CommonModule,
    MatIconModule,
    MatMenuModule,
    DatePipe,
    RouterLink,
    MatButtonModule,
    AmountFormatPipe,
    MatProgressSpinnerModule,
    MatDivider
],
  templateUrl: './recurring-templates-page.component.html',
  styleUrl: './recurring-templates-page.component.scss'
})
export class RecurringTemplatesPageComponent implements OnInit{

  protected isLoading = signal<boolean>(true);
  protected paginationInfo!: Pagination;
  templates: RecurringTemplateSummary[] = [];
  categories: CategorySummary[] = [];
  accounts: AccountSummary[] = [];

  private transactionService = inject(TransactionService);
  private accountService = inject(AccountService);
  private categoryService = inject(CategoryService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  ngOnInit(): void {
    this.loadTemplates();
    this.loadAccounts();
    this.loadCategories();
  }

  private loadTemplates(): void {

    console.log("Loading templates");

    this.transactionService.getRecurringTemplates().subscribe({
      next: (response) => {
        if (response) {
          console.log("Response body data: ", response);

          this.paginationInfo = response.pagination;
          this.templates = response.data;

          this.isLoading.set(false);
        }
      },
      error: (error) => {
        console.log("Error occurred when fetching templates", error);
      }
    });
  }

  get activeCount(): number {
    return this.templates.filter(t => t.isActive).length;
  }

  get inactiveCount(): number {
    return this.templates.filter(t => !t.isActive).length;
  }

  isIncome(t: RecurringTemplateSummary): boolean {
    return t.type === TransactionType.INCOME;
  }

  getFrequencyLabel(value: number, interval: RecurringInterval): string {
    const intervalMap: Record<string, string> = {
      [RecurringInterval.DAY]: 'Day',
      [RecurringInterval.WEEK]: 'Week',
      [RecurringInterval.MONTH]: 'Month',
      [RecurringInterval.YEAR]: 'Year'
    };

    const baseName = intervalMap[interval] || interval;

    if (value === 1) {
      if (interval === RecurringInterval.DAY) return 'Daily';
      return baseName + 'ly'; 
    }
    return `Every ${value} ${baseName}s`;
  }

  onAddRecurringTemplate(): void {

    console.log("Opening add template dialog");

    const dialogRef = this.dialog.open(AddRecurringTemplateDialogComponent, {
      width: '600px',
      data: {
        accounts: this.accounts,
        categories: this.categories
      }
    });

    dialogRef.afterClosed().subscribe( formData => {
      
      console.log("closing dialog");
      if (formData) {
        console.log("Gathered data: ", formData);
        const createReq: RecurringTransactionCreateRequest = RecurringTemplateMapper.toCreateRequest(formData);
        console.log("Data after mapping: ", createReq);

        this.transactionService.createRecurringTemplate(createReq).subscribe({
          next: () => {
            this.loadTemplates();
          },
          error: (error) => {
            console.log("Error occurred when creating transaction: ", error);
          }
        });
      } else {
        console.log("Dialog close without any data");
      }
    });

  }

  onEditTemplate(template: RecurringTemplateSummary) {
    console.log('Edit template:', template.id);
  }

  onChangeStatus(template: RecurringTemplateSummary) {
    console.log('Toggle status for:', template.id);

    this.transactionService.changeTemplateStatus(template.id, !template.isActive).subscribe({
      next: () => {
        console.log("Status changed for: ", !template.isActive);
        template.isActive = !template.isActive;
        let confirmInfo;
        if (template.isActive) {
          confirmInfo = 'activated';
        } else {
          confirmInfo = 'deactivated';
        }
        this.snackBar.open(`Template ${confirmInfo} successfully`, 'close', {
          duration: 3000
        });
      },
      error: (error) => {
        console.log("Error occurred when changing status: ", error);
        this.snackBar.open('Changing template status went wrong. Please try again.', 'close', {
          duration: 3000
        });
      }
    });
  }

  onDeleteTemplate(template: RecurringTemplateSummary) {
    console.log('Delete template:', template.id);
  }

  private loadCategories(): void {
    this.categoryService.getCategories().pipe(
        map(dtos => dtos.map(CategoryMapper.toSummary))
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

  private loadAccounts(): void {
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
}
