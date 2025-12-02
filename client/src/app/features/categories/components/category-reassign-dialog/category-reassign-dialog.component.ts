import { Component, inject } from '@angular/core';
import { CategoryService } from '../../../../core/services/category.service';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Category } from '../../../../core/models/category.model';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

export interface ReassignDialogData {
  categoryToMove: Category;
}

@Component({
  selector: 'app-category-reassign-dialog.component',
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatSelectModule, MatFormFieldModule],
  templateUrl: './category-reassign-dialog.component.html',
  styleUrl: './category-reassign-dialog.component.scss'
})
export class ReassignDialogComponent {
 private categoryService = inject(CategoryService);
  public dialogRef = inject(MatDialogRef<ReassignDialogComponent>);
  public data: ReassignDialogData = inject(MAT_DIALOG_DATA);

  availableCategories: Category[] = [];
  targetCategoryControl = new FormControl<number | null>(null, Validators.required);

  ngOnInit(): void {
    this.categoryService.getCategories(this.data.categoryToMove.type).subscribe(cats => {
      this.availableCategories = cats.filter(c => c.id !== this.data.categoryToMove.id);
    });
  }

  confirm(): void {
    if (this.targetCategoryControl.value) {
      this.dialogRef.close(this.targetCategoryControl.value);
    }
  }
}
