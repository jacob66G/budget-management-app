import { Component, Input, signal, SimpleChanges, ViewChild } from '@angular/core';
import { ChartData, ChartOptions } from 'chart.js';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { CategoryChartPoint } from '../../../../core/models/analytics.model';

@Component({
  selector: 'app-category-sum-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  template: `
    <div class="chart-wrapper">
      @if (hasData()) {
        <canvas baseChart
          [data]="chartData"
          [options]="chartOptions"
          [type]="'doughnut'">
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
      display: flex;
      justify-content: center;
      align-items: center;
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
export class CategorySumChartComponent {
  @Input() data: CategoryChartPoint[] = [];

  hasData = signal(false);

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  chartData: ChartData<'doughnut' | 'pie'> = {
    labels: [],
    datasets: []
  };

  chartOptions: ChartOptions<'doughnut' | 'pie'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'right',
        labels: {
          usePointStyle: true,
          font: { size: 12 }
        }
      },
      tooltip: {
        callbacks: {
          label: (context) => {
            const label = context.label || '';
            const value = context.raw as number;
            return ` ${label}: ${value.toFixed(2)}`;
          }
        }
      }
    },
    cutout: '60%'
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

    const labels = this.data.map(d => d.categoryName);
    const values = this.data.map(d => d.amount);

    const backgroundColors = this.generateColors(this.data.length);

    this.chartData = {
      labels: labels,
      datasets: [
        {
          data: values,
          backgroundColor: backgroundColors,
          hoverOffset: 4,
          borderColor: '#ffffff',
          borderWidth: 2
        }
      ]
    };

    this.chart?.update();
  }

  private generateColors(count: number): string[] {
    const palette = [
      '#3f51b5', // Indigo (Primary)
      '#ff4081', // Pink (Accent)
      '#ffc107', // Amber
      '#009688', // Teal
      '#9c27b0', // Purple
      '#f44336', // Red
      '#4caf50', // Green
      '#795548', // Brown
      '#607d8b', // Blue Grey
      '#2196f3'  // Blue
    ];

    const colors = [];
    for (let i = 0; i < count; i++) {
      colors.push(palette[i % palette.length]);
    }
    return colors;
  }
}
