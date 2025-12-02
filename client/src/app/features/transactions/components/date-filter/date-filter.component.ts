import { Component, effect, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { provideNativeDateAdapter } from '@angular/material/core';
import { FormsModule } from '@angular/forms';
import { DateFilterPresets } from '../../constants/date-filter-presets.enum';
import { 
  startOfWeek, 
  endOfWeek, 
  startOfMonth, 
  endOfMonth, 
  subMonths, 
  subDays, 
  startOfYear, 
  subWeeks,
} from 'date-fns';

export interface DateRange {
  start: Date | null;
  end: Date | null;
}

@Component({
  selector: 'app-date-filter',
  imports: [
    MatMenuModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatInputModule,
    FormsModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './date-filter.component.html',
  styleUrl: './date-filter.component.scss'
})
export class DateFilterComponent {

  readonly maxDate = new Date();
  selectedRange = output<DateRange>();

  start = signal<Date | null>(null);
  end = signal<Date | null>(null);
  activePreset = signal<string | null>(null);

  presetsOptions = [
    {value: DateFilterPresets.LAST_30_DAYS, label: 'Last 30 days'},
    {value: DateFilterPresets.THIS_WEEK, label: 'This week'},
    {value: DateFilterPresets.LAST_WEEK, label: 'Last week'},
    {value: DateFilterPresets.THIS_MONTH, label: 'This month'},
    {value: DateFilterPresets.LAST_MONTH, label: 'Last month'},
    {value: DateFilterPresets.THIS_YEAR, label: 'This year'},
  ]

  clearDates(): void {
    this.start.set(null);
    this.end.set(null);
    this.clearPreset();
  }

  clearPreset(): void {
    this.activePreset.set(null);
  }

  onStartDateChange(): void {
    this.clearPreset();
  }

    onEndDateChange(): void {
    this.clearPreset();
  }

  setPreset(value: DateFilterPresets): void {

    this.activePreset.set(value);

    const range: DateRange = this.calculateDateRange(value);
    this.start.set(range.start);
    this.end.set(range.end);
  }

  calculateDateRange(preset: DateFilterPresets): DateRange {
      
    const now = new Date();
    
    switch (preset) {
      case DateFilterPresets.LAST_30_DAYS:
        return { start: subDays(now, 30), end: now };
        
      case DateFilterPresets.THIS_WEEK:
        return {
          start: startOfWeek(now, {weekStartsOn: 1}),
          end: now
        };

      case DateFilterPresets.LAST_WEEK:
        const lastWeek = subWeeks(now, 1);
        return {
          start: startOfWeek(lastWeek, {weekStartsOn: 1 }),
          end: endOfWeek(lastWeek, {weekStartsOn: 1 })
        };
      
      case DateFilterPresets.THIS_MONTH:
        return {
          start: startOfMonth(now),
          end: now
        };

      case DateFilterPresets.LAST_MONTH:
        const lastMonth = subMonths(now, 1);
        return {
          start: startOfMonth(lastMonth),
          end: endOfMonth(lastMonth)
        };

      case DateFilterPresets.THIS_YEAR:
        return {
          start: startOfYear(now),
          end: now
        };
    }
  }

  submit(): void {
    this.selectedRange.emit({
      start: this.start(),
      end: this.end()
    });
  }
}
