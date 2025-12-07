import { Component, inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { MatSelectModule } from "@angular/material/select";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatIconModule } from "@angular/material/icon";
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RecurringInterval } from '../../constants/recurring-interval.enum';
import { TransactionType } from '../../constants/transaction-type.enum';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { provideNativeDateAdapter } from '@angular/material/core';
import { Title } from 'chart.js';
import { AccountSummary } from '../../model/account-summary.model';
import { CategorySummary } from '../../model/category-summary.model';
import { CategoryType } from '../../../../core/models/category-response-dto.model';
import { MatDivider } from "@angular/material/divider";

@Component({
  selector: 'app-add-recurring-template-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatIconModule,
    ReactiveFormsModule,
    MatNativeDateModule,
    MatButtonModule,
    MatInputModule,
    MatDivider
],
  providers: [
    provideNativeDateAdapter()
  ],
  templateUrl: './add-recurring-template-dialog.component.html',
  styleUrl: './add-recurring-template-dialog.component.scss'
})
export class AddRecurringTemplateDialogComponent implements OnInit{

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<AddRecurringTemplateDialogComponent>);
  readonly data = inject<{accounts: AccountSummary[], categories: CategorySummary[]}>(MAT_DIALOG_DATA);

  categories: CategorySummary[] = this.data.categories;

  addRecurringTemplateForm!: FormGroup;

  protected readonly today = new Date();

  types = [
    { value: TransactionType.EXPENSE, label: 'Expense' },
    { value: TransactionType.INCOME, label: 'Income' }
  ];

  intervals = [
    { value: RecurringInterval.DAY, label: 'Day' },
    { value: RecurringInterval.WEEK, label: 'Week' },
    { value: RecurringInterval.MONTH, label: 'Month' },
    { value: RecurringInterval.YEAR, label: 'Year' }
  ];

  ngOnInit(): void {
    this.addRecurringTemplateForm = this.fb.group({
      // general
      title: ['', [Validators.required, Validators.maxLength(30), Validators.minLength(3)]],
      amount: [0, [Validators.required, Validators.min(0)]],
      type: ['', Validators.required],
      
      // recurrence data
      recurringInterval: [RecurringInterval.MONTH, Validators.required],
      recurringValue: [1, [Validators.required, Validators.min(1), Validators.max(7)]],
      startDate: [new Date(), Validators.required],

      // assignment
      category: ['', Validators.required],
      account: ['', Validators.required],

      // details
      description: ['']
    });
  }

  onReset(): void {
    this.addRecurringTemplateForm.reset({
      Title: '',
      amount: 0,
      type: '',
      recurringInterval: RecurringInterval.MONTH,
      recurringValue: 1,
      startDate: this.today,
      description: ''
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.addRecurringTemplateForm.valid) {
      this.dialogRef.close(this.addRecurringTemplateForm.value);
    }
  }

  onTypeChange(type: TransactionType): void {
    this.categories = this.data.categories.filter( (cat) => {
      return String(cat.type) === String(type) || cat.type === CategoryType.GENERAL;
    });
  }
}
