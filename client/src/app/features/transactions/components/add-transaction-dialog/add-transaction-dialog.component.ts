import { Component, ElementRef, inject, OnInit, signal, ViewChild } from '@angular/core';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { AccountSummary } from '../../model/account-summary.model';
import { CategorySummary } from '../../model/category-summary.model';
import { CategoryType } from '../../../../core/models/category-response-dto.model';
import { MatIconModule } from '@angular/material/icon';
import { FormBuilder, FormGroup, Validators, ɵInternalFormsSharedModule, ReactiveFormsModule } from '@angular/forms';
import { TransactionType } from '../../constants/transaction-type.enum';
import { MatDivider } from "@angular/material/divider";

@Component({
  selector: 'app-add-transaction-dialog',
  imports: [
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    ɵInternalFormsSharedModule,
    ReactiveFormsModule,
    MatDivider
],
  templateUrl: './add-transaction-dialog.component.html',
  styleUrl: './add-transaction-dialog.component.scss'
})
export class AddTransactionDialogComponent implements OnInit{

  addTransactionForm!: FormGroup;

  // signal represents selected File
  selectedFile = signal<File | null>(null);

  // signal represents current photo url to display
  currentPhotoPreviewUrl = signal<string | null>(null);

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  private fb = inject(FormBuilder);

  readonly dialogRef = inject(MatDialogRef<AddTransactionDialogComponent>);
  readonly data = inject<{accountList: AccountSummary[], categoryList: CategorySummary[]}>(MAT_DIALOG_DATA);

  typeOptions = [
    { value: TransactionType.EXPENSE, label: 'Expense'},
    { value: TransactionType.INCOME, label: 'Income'}
  ];

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
    this.resetState();
  }

  onSubmit(): void {
    console.log("closing dialog after submit");
    if (this.addTransactionForm.valid) {
      const result = {transactionData: this.addTransactionForm.getRawValue(), file: this.selectedFile()};
      this.dialogRef.close(result);
    }
  }

  onTransactionTypeChange(event: MatSelectChange): void {
    const chosenType = event.value;
    this.categories = this.data.categoryList.filter( (category) => {
          return String(category.type) === String(chosenType) || category.type === CategoryType.GENERAL;
    })
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (!input.files?.length) return;

    const file = input.files[0];
    this.resetState();

    this.selectedFile.set(file);

    const reader = new FileReader();
    // onLoad will be invoked only when file reading op file succeed
    reader.onload = () => this.currentPhotoPreviewUrl.set(reader.result as string);
    reader.readAsDataURL(file);
  }

  resetState(): void {
    this.selectedFile.set(null);
    this.currentPhotoPreviewUrl.set(null);

    if (this.fileInput?.nativeElement) {
    this.fileInput.nativeElement.value = '';
    }
  }

  onFileRemove(): void {
    this.resetState();
  }
}
