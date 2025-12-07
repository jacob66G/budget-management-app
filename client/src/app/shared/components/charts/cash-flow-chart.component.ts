import { CommonModule, DatePipe } from '@angular/common';
import { Component, inject, Input, OnChanges, signal, SimpleChanges, ViewChild } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { CashFlowChartPoint } from '../../../core/models/analytics.model';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-cash-flow-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  providers: [DatePipe],
  template: `
    <div class="chart-wrapper">
      @if (hasData()) {
        <canvas baseChart
          [data]="barChartData"
          [options]="barChartOptions"
          [type]="'bar'">
        </canvas>
      } @else {
        <div class="no-data-container">
          <p class="no-data">No data for the selected period.</p>
        </div>
      }
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
      height: 100%;
    }
    
    .chart-wrapper {
      position: relative;
      height: 100%; 
      width: 100%;
    }
    
    .no-data-container {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .no-data {
      text-align: center;
      color: #999;
      font-style: italic;
    }
  `]
})
export class CashFlowChartComponent implements OnChanges {
  @Input() data: CashFlowChartPoint[] = [];

  hasData = signal(false);

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;
  
  private datePipe = inject(DatePipe);

  barChartData: ChartData<any> = { labels: [], datasets: [] };

  barChartOptions: ChartOptions<any> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: { usePointStyle: true, padding: 20 }
      },
      tooltip: {
        callbacks: {
          label: (context: any) => {
            const label = context.dataset.label || '';
            const value = context.raw as number;
            return ` ${label}: ${value.toFixed(2)}`;
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: { color: '#f0f0f0' }
      },
      x: {
        grid: { display: false },
        ticks: {
           autoSkip: true,
           maxTicksLimit: 12,
        }
      }
    },
    elements: {
      bar: { borderRadius: 4 }
    }
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data'] && this.data) {
      this.updateChart();
    }
  }

  private updateChart(): void {
    if (!this.data || this.data.length === 0) {
      this.hasData.set(false);
      return;
    }

    const hasValues = this.data.some(d => d.totalIncome > 0 || d.totalExpense > 0);
    if (!hasValues) {
       this.hasData.set(false);
    }

    this.hasData.set(true);

    const labels = this.data.map(d => d.date);
    const isLongRange = this.checkIsLongRange(labels);

    this.barChartOptions = {
        ...this.barChartOptions,
        scales: {
            ...this.barChartOptions.scales,
            x: {
                ...this.barChartOptions.scales!['x'],
                ticks: {
                    ...this.barChartOptions.scales!['x']!.ticks,
                    callback: (val: any, index: number) => {
                        const rawDate = this.barChartData.labels?.[index] as string;
                        if (!rawDate) return '';

                        if (isLongRange) {
                            return this.datePipe.transform(rawDate, 'MMM yyyy') || rawDate;
                        } else {
                            return this.datePipe.transform(rawDate, 'dd.MM') || rawDate;
                        }
                    }
                }
            }
        }
    };

    const incomeData = this.data.map(d => d.totalIncome);
    const expenseData = this.data.map(d => d.totalExpense);

    this.barChartData = {
      labels: labels,
      datasets: [
        {
          data: incomeData,
          label: 'Income',
          backgroundColor: '#4caf50',
          borderColor: '#4caf50',
          hoverBackgroundColor: '#43a047',
          barPercentage: 0.6,
          categoryPercentage: 0.8
        },
        {
          data: expenseData,
          label: 'Expense',
          backgroundColor: '#f44336',
          borderColor: '#f44336',
          hoverBackgroundColor: '#e53935',
          barPercentage: 0.6,
          categoryPercentage: 0.8
        }
      ]
    };

    this.chart?.update();
  }

  private checkIsLongRange(dates: string[]): boolean {
    if (!dates || dates.length < 2) return false;
    
    const start = new Date(dates[0]);
    const end = new Date(dates[dates.length - 1]);
    
    const diffTime = Math.abs(end.getTime() - start.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); 
    
    return diffDays > 90;
  }
}