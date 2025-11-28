import { Component, inject, signal } from '@angular/core';
import { AccountService } from '../../../../core/services/account.service';
import { Account } from '../../../../core/models/account.model';
import { AccountCardComponent } from "../../components/account-card/account-card.component";
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SearchCriteria } from '../../models/account.model';
import { HttpErrorResponse } from '@angular/common/http';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { AccountActionService } from '../../services/account-actions.service';
import { ApiErrorService } from '../../../../core/services/api-error.service';
import { ReferenceDataService } from '../../../../core/services/reference-data.service';

@Component({
  selector: 'app-accounts-list',
  standalone: true,
  imports: [
    AccountCardComponent,
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    AccountCardComponent,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatExpansionModule, MatDatepickerModule, MatNativeDateModule, MatCheckboxModule
  ],
  templateUrl: './accounts-list.html',
  styleUrl: './accounts-list.scss'
})
export class AccountsListPage {
  private accountService = inject(AccountService);
  private accountAction = inject(AccountActionService)
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private errorService = inject(ApiErrorService);
  private refService = inject(ReferenceDataService);

  accounts = signal<Account[]>([]);
  isLoading = signal(true);

  filterForm!: FormGroup;

  currencies = this.refService.currencies;
  budgetTypes = this.refService.budgetTypes;

  ngOnInit() {
    this.initForm();
    this.loadAccounts();

    this.filterForm.valueChanges
      .pipe(
        debounceTime(500),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.loadAccounts();
      });
  }

  private initForm(): void {
    this.filterForm = this.fb.group({
      name: [''],
      sortBy: ['createdAt'],
      sortDirection: ['DESC'],

      currencies: [[]],
      status: [[]],
      budgetTypes: [[]],
      minBalance: [null],
      maxBalance: [null],
      includeInTotalBalance: [null],
      createdAfter: [null],
      createdBefore: [null]
    });
  }

  private loadAccounts() {
    this.isLoading.set(true);

    const criteria: SearchCriteria = this.filterForm.getRawValue();

    this.accountService.getAccounts(criteria).subscribe({
      next: (data) => {
        this.accounts.set(data);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err);
      }
    })
  }

  onAccountClick(id: number): void {
    this.router.navigate(['/app/accounts/', id]);
  }

  onEditClick(id: number): void {
    this.router.navigate(['/app/accounts/edit', id], {state:  {returnUrl: this.router.url}});
  }

  onDeleteClick(account: Account): void {
    this.accountAction.deleteAccount(account, () => this.loadAccounts());
  }

  clearFilters(): void {
    this.filterForm.reset({
      sortBy: 'createdAt',
      sortDirection: 'DESC'
    });
  }

}
