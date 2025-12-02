import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ReferenceDataService } from '../../../../core/services/reference-data.service';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { UpdateCategory } from '../../category.model';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Category } from '../../../../core/models/category.model';

export interface CategoryDialogData {
  mode: 'CREATE' | 'UPDATE';
  category?: Category;
  preselectedType?: string;
}

@Component({
  selector: 'app-category-form-dialog.component',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule
  ],
  templateUrl: './category-form-dialog.component.html',
  styleUrl: './category-form-dialog.component.scss'
})
export class CategoryFormDialogComponent {
  private fb = inject(FormBuilder);
  private refService = inject(ReferenceDataService);
  public dialogRef = inject(MatDialogRef<CategoryFormDialogComponent>);
  public data: CategoryDialogData = inject(MAT_DIALOG_DATA);

  form!: FormGroup;
  availableIcons = this.refService.categoryIcons;
  categoryTypes = this.refService.categoryTypes;

  isTypeDisabled = signal(false);

  ngOnInit(): void {
    const isUpdate = this.data.mode === 'UPDATE';
    const defaultType = isUpdate ? this.data.category?.type : this.data.preselectedType;
    this.isTypeDisabled.set(isUpdate);

    this.form = this.fb.group({
      name: [this.data.category?.name || '', [Validators.required, Validators.minLength(2)]],
      type: [{ value: defaultType, disabled: isUpdate }, Validators.required],
      iconPath: [this.data.category?.iconPath || '', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const result: UpdateCategory = {
      name: this.form.value.name,
      isDefault: false,
      iconPath: this.form.value.iconPath,
      type: this.form.value.type
    };

    this.dialogRef.close(result);
  }

  getIconName(url: string): string {
    if (!url) return '';
    const filename = url.substring(url.lastIndexOf('/') + 1);
    const name = filename.split('.')[0];
    return name.charAt(0).toUpperCase() + name.slice(1);
  }
}
