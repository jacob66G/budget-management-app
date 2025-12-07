import { Component, DestroyRef, inject, signal } from '@angular/core';
import { AccountService } from '../../../../core/services/account.service';
import { ApiErrorService } from '../../../../core/services/api-error.service';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AccountDetails, CreateAccount } from '../../models/account.model';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDivider } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSliderModule } from '@angular/material/slider';
import { ReferenceDataService } from '../../../../core/services/reference-data.service';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-account-add-page',
  standalone: true,
  imports: [
    CommonModule,
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
  templateUrl: './account-add.page.html',
  styleUrl: './account-add.page.scss'
})
export class AccountAddPage {
  private accountService = inject(AccountService)
  private errorSevice = inject(ApiErrorService)
  private refService = inject(ReferenceDataService);
  private router = inject(Router)
  private fb = inject(FormBuilder)
  private snackBar = inject(MatSnackBar)
  private destroyRef = inject(DestroyRef);

  isLoading = signal(false);

  currencies = this.refService.currencies;
  budgetTypes = this.refService.budgetTypes;
  accountIcons = this.refService.accountIcons;
  accountTypes = this.refService.accountTypes;

  createForm = this.fb.group({
    type: ['', Validators.required],
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    currency: ['', Validators.required],
    description: ['', Validators.maxLength(255)],
    initialBalance: [0, [Validators.required, Validators.min(0)]],
    budgetType: ['NONE', Validators.required],
    alertThreshold: [null as number | null, [Validators.min(0), Validators.max(100)]],
    budget: [null as number | null, [Validators.min(0)]],
    includeInTotalBalance: [true, Validators.required],
    iconPath: ['']
  })

  ngOnInit(): void {
    this.handleBudgetTypeChange('NONE');

    this.createForm.controls.budgetType.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(value => {
        this.handleBudgetTypeChange(value);
      });
  }

  onSubmit() {
    if (!this.createForm.valid) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const dto = this.createForm.value as CreateAccount;

    this.accountService.createAccount(dto).subscribe({
      next: (data: AccountDetails) => {
        this.isLoading.set(false);
        this.snackBar.open('The account have been created successfully', 'OK', { duration: 3000, panelClass: 'success-snackbar' });
        this.router.navigate(['/app/accounts/', data.id]);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorSevice.handle(err);
      }
    })
  }

  onCancel() {
    this.router.navigate(['/app/accounts']);
  }

  getIconName(url: string): string {
    if (!url) return '';
    const filename = url.substring(url.lastIndexOf('/') + 1);
    const name = filename.split('.')[0];
    return name.charAt(0).toUpperCase() + name.slice(1);
  }

 private handleBudgetTypeChange(type: string | null): void {
    const budgetControl = this.createForm.controls.budget;
    const alertControl = this.createForm.controls.alertThreshold;

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
}
