import { Component, inject, signal } from '@angular/core';
import { AccountService } from '../../../../core/services/account.service';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { of, switchMap } from 'rxjs';
import { AccountDetails, UpdateAccount } from '../../models/account.model';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
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
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);
  private errorService = inject(ApiErrorService);
  private refService = inject(ReferenceDataService);
  private returnUrl = '/app/accounts';

  editForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    currency: ['', Validators.required],
    description: ['', Validators.maxLength(255)],
    initialBalance: [0, Validators.min(0)],
    budgetType: ['', Validators.required],
    budget: [0, Validators.min(0)],
    alertThreshold: [0, [Validators.min(0), Validators.max(100)]],
    includeInTotalBalance: [true, Validators.required],
    iconPath: []
  });

  account = signal<AccountDetails | null>(null);
  isLoading = signal(true);

  currencies = this.refService.currencies;
  budgetTypes = this.refService.budgetTypes;
  accountIcons = this.refService.accountIcons;

  constructor() {
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
        this.snackBar.open('The account have been modified successfully', 'OK', { duration: 3000, panelClass: 'success-snackbar' });
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
    })
  }
}
