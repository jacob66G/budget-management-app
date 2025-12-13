import { Component, DestroyRef, inject, signal } from '@angular/core';
import { AccountService } from '../../../../core/services/account.service';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { of, switchMap } from 'rxjs';
import { AccountDetails, UpdateAccount } from '../../models/account.model';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiErrorService } from '../../../../core/services/api-error.service';
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDivider } from "@angular/material/divider";
import { MatCardModule } from '@angular/material/card';
import { MatSliderModule } from '@angular/material/slider';
import { ReferenceDataService } from '../../../../core/services/reference-data.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ToastService } from '../../../../core/services/toast-service';

@Component({
  selector: 'app-account-edit.page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatExpansionModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCheckboxModule,
    MatDivider,
    MatSliderModule
  ],
  templateUrl: './account-edit.page.html',
  styleUrl: './account-edit.page.scss'
})
export class AccountEditPage {
  private accountService = inject(AccountService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private errorService = inject(ApiErrorService);
  private refService = inject(ReferenceDataService);
  private destroyRef = inject(DestroyRef);
  private toastService = inject(ToastService)
  
  private returnUrl = '/app/accounts';

  editForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    currency: ['', Validators.required],
    description: ['', Validators.maxLength(255)],
    initialBalance: [0],
    budgetType: ['', Validators.required],
    budget: [null as number | null, Validators.min(0)],
    alertThreshold: [null as number | null, [Validators.min(0), Validators.max(100)]],
    includeInTotalBalance: [true, Validators.required],
    iconPath: ['']
  });

  account = signal<AccountDetails | null>(null);
  isLoading = signal(true);

  currencies = this.refService.currencies;
  budgetTypes = this.refService.budgetTypes;
  accountIcons = this.refService.accountIcons;

  constructor() {
    this.setupFormListeners();

    const navigation = this.router.currentNavigation();
    const stateReturnUrl = navigation?.extras?.state?.['returnUrl'];
    if (stateReturnUrl) {
      this.returnUrl = stateReturnUrl;
    }

    const stateAccount = navigation?.extras?.state?.['accountData'] as AccountDetails;
    if (stateAccount) {
      this.account.set(stateAccount);
      this.fillForm(stateAccount);
      this.isLoading.set(false);
    }
  }

  ngOnInit(): void {
    if (!this.account()) {
      this.loadAccount();
    }
  }

  onSubmit(): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    const accountData = this.account();
    if (!accountData) return;

    this.isLoading.set(true);
    const dto = this.editForm.value as UpdateAccount;

    this.accountService.updateAccount(accountData.id, dto).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.toastService.showSuccess('The account have been modified successfully');
        this.router.navigate(['/app/accounts/', accountData.id]);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err);
      }
    })
  }

  onCancel() {
    this.router.navigateByUrl(this.returnUrl);
  }

  getIconName(url: string): string {
    if (!url) return '';
    const filename = url.substring(url.lastIndexOf('/') + 1);
    const name = filename.split('.')[0];
    return name.charAt(0).toUpperCase() + name.slice(1);
  }

  private loadAccount() {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        const idString = params.get('id');

        if (!idString) {
          this.isLoading.set(true);
          this.router.navigate(['/app/accounts']);
          return of(null);
        }

        const accountId = Number(idString);

        this.isLoading.set(true);
        return this.accountService.getAccount(accountId);

      })
    ).subscribe({
      next: (accountData: AccountDetails | null) => {
        this.account.set(accountData);
        if (accountData) {
          this.fillForm(accountData);
        }
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.account.set(null);

        this.errorService.handle(err);
      }
    })
  }

  private setupFormListeners() {
    this.editForm.controls['budgetType'].valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((type: string) => {
        this.handleBudgetTypeChange(type);
      });
  }

  private handleBudgetTypeChange(type: string | null): void {
    const budgetControl = this.editForm.controls['budget'];
    const alertControl = this.editForm.controls['alertThreshold'];

    if (type === 'NONE') {
      budgetControl.setValue(null, { emitEvent: false });
      budgetControl.disable({ emitEvent: false });

      alertControl.setValue(null, { emitEvent: false });
      alertControl.disable({ emitEvent: false });
    } else {
      budgetControl.enable({ emitEvent: false });
      alertControl.enable({ emitEvent: false });

      if (budgetControl.value === null) {
        budgetControl.setValue(0, { emitEvent: false });
      }
      if (alertControl.value === null) {
        alertControl.setValue(0, { emitEvent: false });
      }
    }
  }

  private fillForm(data: AccountDetails) {
    this.editForm.patchValue({
      name: data.name,
      currency: data.currency,
      description: data.description,
      initialBalance: data.balance,
      budgetType: data.budgetType,
      budget: data.budget,
      alertThreshold: data.alertThreshold,
      includeInTotalBalance: data.includeInTotalBalance,
      iconPath: data.iconPath
    }, { emitEvent: false });

      this.handleBudgetTypeChange(data.budgetType); 
  }

  hasChanges(): boolean {
  const original = this.account();
  if (!original) return false;
  const current = this.editForm.getRawValue();

  if (current.name != original.name) return true;
  if (current.currency != original.currency) return true;
  if (current.description != original.description) return true;
  if (current.iconPath != original.iconPath) return true;
  if (current.budgetType != original.budgetType) return true
  if (current.budgetType !== 'NONE') {
      if (current.budget != original.budget) return true;
      if (current.alertThreshold != original.alertThreshold) return true;
  }
  if (current.includeInTotalBalance != original.includeInTotalBalance) return true;

  if (!original.hasTransactions) {
      if (current.initialBalance != original.balance) return true;
  }

  return false;
}
}
