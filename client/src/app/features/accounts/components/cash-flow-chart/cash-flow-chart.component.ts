import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, signal, SimpleChanges, ViewChild } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { CashFlowChartPoint } from '../../../../core/models/analytics.model';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-cash-flow-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
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
    .chart-wrapper {
      position: relative;
      height: 500px;
      width: 1000px;
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
  @Input() currency: string = 'PLN';

  hasData = signal(false);

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  barChartData: ChartData<any> = {
    labels: [],
    datasets: []
  };

  barChartOptions: ChartOptions<any> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          usePointStyle: true,
          padding: 20
        }
      },
      tooltip: {
        callbacks: {
          label: (context: any) => {
            const label = context.dataset.label || '';
            const value = context.raw as number;
            return ` ${label}: ${value.toFixed(2)} ${this.currency}`;
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
        grid: { display: false }
      }
    },
    elements: {
      bar: {
        borderRadius: 4
      }
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

    this.hasData.set(true);

    const labels = this.data.map(d => d.date);

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
}