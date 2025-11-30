import { Component, Input, signal, SimpleChanges, ViewChild } from '@angular/core';
import { ChartPoint } from '../../../../core/models/analytics.model';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'app-balance-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  template: `
  <div class="chart-wrapper">
      @if (hasData()) {
        <canvas baseChart
          [data]="lineChartData"
          [options]="lineChartOptions"
          [type]="'line'">
        </canvas>
      } @else {
        <p class="no-data">No data for the selected period.</p>
      }
    </div>
  `,
  styles: [`
    .chart-wrapper {
      position: relative;
      height: 500px;
      width: 1000px;
    }
    .no-data {
      text-align: center;
      color: #999;
      padding-top: 100px;
    }
  `]
})
export class BalanceChartComponent {
  @Input() data: ChartPoint[] = [];
  @Input() currency: string = 'PLN';

  hasData = signal(false);

  lineChartData: ChartConfiguration<'line'>['data'] = {
    datasets: [],
    labels: []
  };

  lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (context) => {
            return `Saldo: ${context.raw} ${this.currency}`;
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: false,
        grid: { color: '#f0f0f0' }
      },
      x: {
        grid: { display: false }
      }
    },
    elements: {
      line: {
        tension: 0.4,
        borderColor: '#3f51b5',
        backgroundColor: 'rgba(63, 81, 181, 0.1)',
        fill: true
      },
      point: {
        radius: 3,
        hoverRadius: 6
      }
    }
  };

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

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

    this.lineChartData = {
      labels: this.data.map(p => p.date),
      datasets: [
        {
          data: this.data.map(p => p.amount),
          label: 'Saldo',
          borderColor: '#3f51b5',
          backgroundColor: 'rgba(63, 81, 181, 0.1)',
          pointBackgroundColor: '#fff',
          pointBorderColor: '#3f51b5',
        }
      ]
    };

    this.chart?.update();
  }
}
