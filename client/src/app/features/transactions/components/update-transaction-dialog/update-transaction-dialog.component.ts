import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TransactionUpdateData } from '../../model/transaction-update-data.model';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-update-transaction-dialog',
  imports: [MatDialogModule, MatFormFieldModule, ReactiveFormsModule, MatInputModule, MatButtonModule],
  templateUrl: './update-transaction-dialog.component.html',
  styleUrl: './update-transaction-dialog.component.scss'
})
export class UpdateTransactionDialogComponent implements OnInit{

  updateTransactionForm!: FormGroup;
  private fb = inject(FormBuilder);

  readonly dialogRef = inject(MatDialogRef<UpdateTransactionDialogComponent>);
  readonly data = inject<TransactionUpdateData>(MAT_DIALOG_DATA);

  ngOnInit(): void {
    this.updateTransactionForm = this.fb.group({
      title: [this.data.title, Validators.required],
      amount: [this.data.amount, [Validators.required, Validators.min(0)]],
      description: [this.data.description]
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onReset(): void {
    this.updateTransactionForm.reset(this.data);
  }

  get isUnchanged(): boolean {
    const formValue = this.updateTransactionForm.value;
    return JSON.stringify(formValue) === JSON.stringify(this.data);
  }

  onSubmit(): void {
    if (this.updateTransactionForm.valid) {
      this.dialogRef.close(this.updateTransactionForm.value);
    }
  }
}
