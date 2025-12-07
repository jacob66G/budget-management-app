import { Component, inject, Input, OnChanges, signal, SimpleChanges, ViewChild } from '@angular/core';
import { ChartPoint, MultiSeriesChart } from '../../../core/models/analytics.model';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { CommonModule, DatePipe } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'app-balance-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  providers: [DatePipe],
  template: `
    <div class="chart-wrapper">
      @if (hasData()) {
        <canvas baseChart
          [data]="lineChartData"
          [options]="lineChartOptions"
          [type]="'line'">
        </canvas>
      } @else {
        <div class="no-data-container">
           <p class="no-data">No data available for the selected period.</p>
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
    .chart-wrapper { position: relative; height: 100%; width: 100%; }
    .no-data-container { height: 100%; display: flex; align-items: center; justify-content: center; }
    .no-data { color: #999; font-style: italic; }
  `]
})
export class BalanceChartComponent implements OnChanges {
  @Input() data: ChartPoint[] = [];
  @Input() multiSeriesData: MultiSeriesChart | null = null;
  @Input() currency: string = 'PLN';
  @Input() isStacked: boolean = false;

  hasData = signal(false);
  
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;
  
  private datePipe = inject(DatePipe);

  lineChartData: ChartConfiguration<'line'>['data'] = { datasets: [], labels: [] };

  lineChartOptions: ChartOptions<'line'> = {
     responsive: true,
     maintainAspectRatio: false,
     plugins: {
       legend: { display: true },
       tooltip: {
         mode: 'index',
         intersect: false,
         callbacks: {
           label: (context) => {
              let label = context.dataset.label || '';
              if (label) label += ': ';
              return `${label}${context.raw} ${this.currency}`;
           }
         }
       }
     },
     scales: {
        y: { 
           beginAtZero: false, 
           stacked: false,
           grid: { color: '#f0f0f0' }
        },
        x: {
           grid: { display: false },
           ticks: {
              maxRotation: 45,
              minRotation: 0,
              autoSkip: true,
              maxTicksLimit: 12
           }
        }
     },
     elements: {
       line: { tension: 0.4, fill: false },
       point: { radius: 2, hoverRadius: 5 }
     }
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data'] || changes['multiSeriesData']) {
      this.updateChart();
    }
  }

  private updateChart(): void {
    let labels: string[] = [];
    if (this.lineChartOptions.scales?.['y']) {
        this.lineChartOptions.scales['y'].stacked = this.isStacked;
    }

    if (this.multiSeriesData && this.multiSeriesData.series.length > 0) {
       this.hasData.set(true);
       

       labels = this.multiSeriesData.dates; 

       this.lineChartData = {
         labels: labels, 
         datasets: this.multiSeriesData.series.map((series, index) => ({
            data: series.data,
            label: series.label,
            fill: this.isStacked ? 'origin' : false,
            borderColor: this.getColor(index),
            backgroundColor: this.getTransparentColor(index),
            pointBackgroundColor: '#fff',
            pointBorderColor: this.getColor(index),
         }))
       };
    } else if (this.data && this.data.length > 0) {
       this.hasData.set(true);
       labels = this.data.map(p => p.date);
       
       this.lineChartData = {
         labels: labels,
         datasets: [{
            data: this.data.map(p => p.amount),
            label: 'Balance',
            borderColor: '#3f51b5',
            backgroundColor: 'rgba(63, 81, 181, 0.1)',
            fill: true
         }]
       };
    } else {
       this.hasData.set(false);
       return;
    }
  
    this.updateXAxisOptions(labels);

    this.chart?.update();
  }

  private updateXAxisOptions(labels: string[]): void {
      const isLongRange = this.checkIsLongRange(labels);
      
      this.lineChartOptions = {
        ...this.lineChartOptions,
        scales: {
          ...this.lineChartOptions.scales,
          x: {
             ...this.lineChartOptions.scales!['x'],
             ticks: {
                ...this.lineChartOptions.scales!['x']!.ticks,
                callback: (val, index) => {
                   const rawDate = labels[index] as string;
                   
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
  }

  private checkIsLongRange(dates: string[]): boolean {
    if (!dates || dates.length < 2) return false;
    
    const start = new Date(dates[0]);
    const end = new Date(dates[dates.length - 1]);
    
    const diffTime = Math.abs(end.getTime() - start.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); 
    
    return diffDays > 90;
  }

  private getColor(index: number): string {
    const colors = ['#3f51b5', '#ff4081', '#009688', '#ffc107', '#9c27b0', '#f44336'];
    return colors[index % colors.length];
  }

  private getTransparentColor(index: number): string {
    const hex = this.getColor(index);
    return hex + '33';
  }
}