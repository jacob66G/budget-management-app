import { Component, inject, OnInit } from '@angular/core';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatCardActions } from "@angular/material/card";
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { TransactionType } from '../../constants/transaction-type.enum';
import { AccountSummary } from '../../model/account-summary.model';
import { CategorySummary } from '../../model/category-summary.model';
import { CategoryType } from '../../../../core/models/category-response-dto.model';
import { FormBuilder, FormGroup, Validators, ɵInternalFormsSharedModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-add-transaction-dialog',
  imports: [MatCardActions, MatButtonModule, MatDialogModule, MatInputModule, MatFormFieldModule, MatSelectModule, ɵInternalFormsSharedModule, ReactiveFormsModule],
  templateUrl: './add-transaction-dialog.component.html',
  styleUrl: './add-transaction-dialog.component.css'
})
export class AddTransactionDialogComponent implements OnInit{

  addTransactionForm!: FormGroup;

  private fb = inject(FormBuilder);

  readonly dialogRef = inject(MatDialogRef<AddTransactionDialogComponent>);
  readonly data = inject<{accountList: AccountSummary[], categoryList: CategorySummary[], transactionTypes: string[]}>(MAT_DIALOG_DATA);

  categories: CategorySummary[] = this.data.categoryList;

  ngOnInit(): void {
    this.addTransactionForm = this.fb.group({
      title: ['', Validators.required],
      amount: [0, [Validators.required, Validators.min(0)]],
      type: ['', Validators.required],
      description: [''],
      account: ['', Validators.required],
      category: ['', Validators.required],
    });
  }

  clearForm(): void {
    this.addTransactionForm.reset({
      title: '',
      amount: 0,
      type: '',
      description: '',
      account: '',
      category: '',
    });
    this.categories = this.data.categoryList;
  }

  onSubmit(): void {
    console.log("closing dialog after submit");
    if (this.addTransactionForm.valid) {
       this.dialogRef.close(this.addTransactionForm.value);
    }
  }

  onTransactionTypeChange(event: MatSelectChange): void {
    const chosenType = event.value;
    this.categories = this.data.categoryList.filter( (category) => {
          return String(category.type) === String(chosenType) || category.type === CategoryType.GENERAL;
    })
  }
}
