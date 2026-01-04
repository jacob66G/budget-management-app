import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { provideNativeDateAdapter } from '@angular/material/core';
import { E } from '@angular/cdk/keycodes';

type RangeType = 'current_month' | 'last_month' | 'last_3_months' | 'last_6_months' | 'current_year' | 'last_year' | 'custom';

interface RangeOption {
  value: RangeType;
  label: string;
}

@Component({
  selector: 'app-report-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatInputModule,
    MatButtonModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './report-dialog.html',
  styleUrl: './report-dialog.scss'
})
export class ReportDialog implements OnInit {
  private fb = inject(FormBuilder);
  public dialogRef = inject(MatDialogRef<ReportDialog>);

  rangeOptions: RangeOption[] = [
    { value: 'current_month', label: 'Current month' },
    { value: 'last_month', label: 'Last month' },
    { value: 'last_3_months', label: 'Last 3 months' },
    { value: 'last_6_months', label: 'Last 3 months' },
    { value: 'current_year', label: 'Current year' },
    { value: 'last_year', label: 'Last year' },
    { value: 'custom', label: 'Custom period' }
  ];

  form = this.fb.group({
    rangeType: new FormControl<RangeType>('current_month', { nonNullable: true }),
    dateRange: this.fb.group({
      start: new FormControl<Date | null>(null, Validators.required),
      end: new FormControl<Date | null>(null, Validators.required),
    })
  });

  ngOnInit(): void {
    this.form.controls.rangeType.valueChanges.subscribe((value) => {
      this.updateDateRange(value);
    });

    this.updateDateRange('current_month');
  }

  private updateDateRange(range: RangeType): void {
    const rangeControl = this.form.controls.dateRange;
    const now = new Date();
    let start: Date | null = new Date();
    let end: Date | null = new Date(); 

    switch (range) {
      case 'current_month':
        start = new Date(now.getFullYear(), now.getMonth(), 1);
        end = now;
        break;

      case 'last_month':
        start = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        end = new Date(now.getFullYear(), now.getMonth(), 0);
        break;

      case 'last_3_months':
        start = new Date(now.getFullYear(), now.getMonth() - 3, 1);
        end = now;
        break;

      case 'last_6_months':
        start = new Date(now.getFullYear(), now.getMonth() - 6, 1);
        end = now;
        break;

      case 'current_year':
        start = new Date(now.getFullYear(), 0, 1);
        end = now;
        break;

      case 'last_year':
        start = new Date(now.getFullYear() - 1, 0, 1);
        end = new Date(now.getFullYear() - 1, 11, 31);
        break;

      case 'custom':
        rangeControl.enable();
        return;
    }

    if (start && end) {
      rangeControl.setValue({ start, end });
      rangeControl.disable();
    }
  }

  generate(): void {
    if (this.form.valid) {
      const result = this.form.getRawValue();
      this.dialogRef.close({
        start: result.dateRange.start,
        end: result.dateRange.end
      });
    }
  }
}