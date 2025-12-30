import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TransactionUpdateData } from '../../model/transaction-update-data.model';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from "@angular/material/icon";
import { MatSelectModule } from '@angular/material/select';
import { MatDivider } from "@angular/material/divider";

@Component({
  selector: 'app-update-transaction-dialog',
  imports: [
    MatDialogModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDivider
],
  templateUrl: './update-transaction-dialog.component.html',
  styleUrl: './update-transaction-dialog.component.scss'
})
export class UpdateTransactionDialogComponent implements OnInit{

  updateTransactionForm!: FormGroup;
  private fb = inject(FormBuilder);

  readonly dialogRef = inject(MatDialogRef<UpdateTransactionDialogComponent>);
  readonly data = inject<TransactionUpdateData>(MAT_DIALOG_DATA);

  selectedFile = signal<File | null>(null);
  currentPhotoPreviewUrl = signal<string | null>(null);

  ngOnInit(): void {
    this.updateTransactionForm = this.fb.group({
      title: [this.data.title, Validators.required],
      amount: [this.data.amount, [Validators.required, Validators.min(0)]],
      description: [this.data.description],
      categoryId: [
        {
          value: this.data.categoryId,
          disabled: this.data.isRecurring
        },
        Validators.required] 
    });
    if (this.data.attachmentData) this.currentPhotoPreviewUrl.set(this.data.attachmentData.downloadUrl);
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onReset(): void {
    this.updateTransactionForm.reset({
      title: this.data.title,
      amount: this.data.amount,
      description: this.data.description,
      categoryId: this.data.categoryId
    });
    this.resetAttachmentState();
  }

  isUnchanged(): boolean {
    const formValue = this.updateTransactionForm.value;

    return (
      formValue.title === this.data.title &&
      formValue.amount === this.data.amount &&
      formValue.description === this.data.description &&
      formValue.categoryId === this.data.categoryId &&
      this.selectedFile() == null
    );
  }

  onSubmit(): void {
    if (this.updateTransactionForm.valid) {
      const result = {transactionData: this.updateTransactionForm.getRawValue(), file: this.selectedFile()}
      this.dialogRef.close(result);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (!input.files?.length) return;

    const file = input.files[0];
    this.resetAttachmentState();

    this.selectedFile.set(file);

    const reader = new FileReader();
    // onLoad will be invoked only when file reading op file succeed
    reader.onload = () => this.currentPhotoPreviewUrl.set(reader.result as string);
    reader.readAsDataURL(file);
  }

  onFileRemove(): void {
    this.selectedFile.set(null);
    this.currentPhotoPreviewUrl.set(null);
  }

  resetAttachmentState(): void {
    this.selectedFile.set(null);
    this.currentPhotoPreviewUrl.set(this.data.attachmentData?.downloadUrl || null);
  }
}
