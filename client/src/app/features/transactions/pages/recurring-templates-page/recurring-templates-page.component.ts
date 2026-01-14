import { Component, inject, OnInit, signal } from '@angular/core';
import { UpdateRecurringTemplateDialogComponent } from '../../components/update-recurring-template-dialog/update-recurring-template-dialog.component';
import { TransactionType } from '../../constants/transaction-type.enum';
import { RecurringInterval } from '../../constants/recurring-interval.enum';
import { RecurringTemplateSummary } from '../../model/recurring-template-summary.model';
import { MatIconModule } from "@angular/material/icon";
import { MatMenuModule } from "@angular/material/menu";
import { MatButtonModule } from '@angular/material/button';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AmountFormatPipe } from '../../../../shared/pipes/amount-format-pipe';
import { TransactionService } from '../../../../core/services/transaction.service';
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
import { CategoryIconComponent } from "../../../../shared/components/category-icon/category-icon.component/category-icon.component";
import { RecurringTemplateUpdateRequest } from '../../model/recurring-template-update-request.model';
import { UpdateRange } from '../../constants/update-range.enum';
import { ConfirmDialog } from '../../../../shared/components/dialogs/confirm-dialog/confirm-dialog';
import { RemovalRange } from '../../constants/removal-range.enum';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorService } from '../../../../core/services/api-error.service';

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
    MatDivider,
    CategoryIconComponent
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
  private errorService = inject(ApiErrorService);

  ngOnInit(): void {
    this.loadTemplates();
    this.loadAccounts();
    this.loadCategories();
  }

  private loadTemplates(): void {

    this.transactionService.getRecurringTemplates().subscribe({
      next: (response) => {
        if (response) {
          this.paginationInfo = response.pagination;
          this.templates = response.data;
          this.isLoading.set(false);
        }
      },
      error: (error: HttpErrorResponse) => this.errorService.handle(error)
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

    const dialogRef = this.dialog.open(AddRecurringTemplateDialogComponent, {
      width: '600px',
      maxHeight: '85vh',
      data: {
        accounts: this.accounts,
        categories: this.categories
      }
    });

    dialogRef.afterClosed().subscribe( formData => {
      if (formData) {
        const createReq: RecurringTransactionCreateRequest = RecurringTemplateMapper.toCreateRequest(formData);
        this.transactionService.createRecurringTemplate(createReq).subscribe({
          next: () => {
            this.loadTemplates();
          },
          error: (error: HttpErrorResponse) => this.errorService.handle(error)
        });
      }
    });

  }

  onEditTemplate(template: RecurringTemplateSummary) {
    const dialogRef = this.dialog.open(UpdateRecurringTemplateDialogComponent, {
      width: '500px',
      maxHeight: '90vh',
      data: template
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {        
        const updateRequest: RecurringTemplateUpdateRequest = {
          title: result.title,
          amount: result.amount,
          description: result.description
        };

        this.transactionService.updateRecurringTemplate(result.id, updateRequest, UpdateRange.FUTURE).subscribe({
          next: () => {
            this.loadTemplates();
            this.snackBar.open('Template updated successfully', 'Close', {
              duration: 3000
            });
          },
          error: (error: HttpErrorResponse) => this.errorService.handle(error)
        });
      }
    });

  }

  onChangeStatus(template: RecurringTemplateSummary) {
    this.transactionService.changeTemplateStatus(template.id, !template.isActive).subscribe({
      next: () => {
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
      error: (error: HttpErrorResponse) => this.errorService.handle(error)
    });
  }

  onDeleteTemplate(template: RecurringTemplateSummary) {
        const dialogRef = this.dialog.open(ConfirmDialog, {
          width: '400px',
          data: {
            title: 'Delete Recurring Template',
            message: 'Are you sure you want to delete this template? This action cannot be undone.',
            confirmButtonText: 'Delete',
            confirmButtonColor: 'warn'
          }
        });
    
        dialogRef.afterClosed().subscribe((result: boolean) => {
    
          if (result === true) {
    
            this.transactionService.deleteRecurringTemplate(template.id, RemovalRange.TEMPLATE).subscribe({
    
              next: () => {
                this.loadTemplates();
    
                this.snackBar.open('Transaction deleted successfully', 'Close', {
                  duration: 3000
                });
              },
              error: (error: HttpErrorResponse) => this.errorService.handle(error)
            });
          }
        });
  }

  private loadCategories(): void {
    this.categoryService.getCategories().pipe(
        map(dtos => dtos.map(CategoryMapper.toSummary))
    )
    .subscribe({
      next: (data) => {
        this.categories = data;
      },
      error: (error: HttpErrorResponse) => this.errorService.handle(error)
    });
  }

  private loadAccounts(): void {
    this.accountService.getAccounts().pipe(
      map(dtos => dtos.map(AccountMapper.toSummary))
    )
    .subscribe({
      next: (data) => {
        this.accounts = data;
      },
      error: (error: HttpErrorResponse) => this.errorService.handle(error)
    });
  }
}
