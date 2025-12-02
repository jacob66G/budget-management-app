import { Component, inject, signal } from '@angular/core';
import { CategoryService } from '../../../../core/services/category.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Category } from '../../../../core/models/category.model';
import { CategoryDialogData, CategoryFormDialogComponent } from '../../components/category-form-dialog/category-form-dialog.component';
import { UpdateCategory } from '../../category.model';
import { ReassignDialogComponent, ReassignDialogData } from '../../components/category-reassign-dialog/category-reassign-dialog.component';
import { ConfirmDialog, ConfirmDialogData } from '../../../../shared/components/dialogs/confirm-dialog/confirm-dialog';
import { filter, switchMap } from 'rxjs';
import { CategoryCardComponent } from '../../components/category-card/category-card.component';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { ApiErrorService } from '../../../../core/services/api-error.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-category-page',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    CategoryCardComponent,
    MatChipsModule
  ],
  templateUrl: './category-page.component.html',
  styleUrl: './category-page.component.scss'
})
export class CategoryPageComponent {
  private categoryService = inject(CategoryService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private errorService = inject(ApiErrorService)

  categories = signal<Category[]>([]);
  currentType = signal<string | null>(null);
  isLoading = signal(true);

  onFilterChange(type: string | null): void {
    this.currentType.set(type);
    this.loadCategories();
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading.set(true);

    const typeParam = this.currentType() || undefined;

    this.categoryService.getCategories(typeParam).subscribe({
      next: (data) => {
        this.categories.set(data);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err);
      }
    });
  }

  onTabChange(index: number): void {
    const types = ['EXPENSE', 'INCOME', 'GENERAL'];
    this.currentType.set(types[index]);
    this.loadCategories();
  }


  openAddDialog(): void {
    const dialogRef = this.dialog.open(CategoryFormDialogComponent, {
      width: '450px',
      data: { mode: 'CREATE', preselectedType: this.currentType() } as CategoryDialogData
    });

    dialogRef.afterClosed().subscribe((result: UpdateCategory) => {
      if (result) {
        this.categoryService.createCategory(result).subscribe({
          next: () => {
            this.snackBar.open('Category added', 'OK', { duration: 3000 });
            this.loadCategories();
          },
          error: (err: HttpErrorResponse) => {
            this.isLoading.set(false);
            this.errorService.handle(err);
          }
        })
      }
    });
  }

  onEdit(category: Category): void {
    const dialogRef = this.dialog.open(CategoryFormDialogComponent, {
      width: '450px',
      data: { mode: 'UPDATE', category } as CategoryDialogData
    });

    dialogRef.afterClosed().subscribe((result: UpdateCategory) => {
      if (result) {
        this.categoryService.updateCategory(category.id, result).subscribe({
          next: () => {
            this.snackBar.open('Category updated', 'OK', { duration: 3000 });
            this.loadCategories();
          },
          error: (err: HttpErrorResponse) => {
            this.isLoading.set(false);
            this.errorService.handle(err);
          }
        });
      }
    });
  }

  onReassign(category: Category): void {
    const dialogRef = this.dialog.open(ReassignDialogComponent, {
      width: '450px',
      data: { categoryToMove: category } as ReassignDialogData
    });

    dialogRef.afterClosed().subscribe((newCategoryId: number) => {
      if (newCategoryId) {
        this.categoryService.reassignCategory(category.id, newCategoryId).subscribe({
          next: () => {
            this.snackBar.open('Transactions transferred', 'OK', { duration: 3000 });
          },
          error: (err: HttpErrorResponse) => {
            this.isLoading.set(false);
            this.errorService.handle(err);
          }
        });
      }
    });
  }

  onDelete(category: Category): void {
    this.dialog.open(ConfirmDialog, {
      data: {
        title: 'Remove category?',
        message: `Are you sure you want to delete ${category.name}? Make sure there are no assigned transactions.`,
        confirmButtonColor: 'warn',
        confirmButtonText: 'Delete'
      } as ConfirmDialogData
    }).afterClosed().pipe(
      filter(res => res === true),
      switchMap(() => this.categoryService.deleteCategory(category.id))
    ).subscribe({
      next: () => {
        this.snackBar.open('Category deleted', 'OK', { duration: 3000 });
        this.loadCategories();
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorService.handle(err);
      }
    });
  }
}
