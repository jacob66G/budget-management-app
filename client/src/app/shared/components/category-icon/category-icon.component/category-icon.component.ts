import { CommonModule } from '@angular/common';
import { Component, computed, input, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-category-icon',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
  <div class="icon-wrapper" [ngClass]="typeClass()">
      @if (iconUrl() && !hasError()) {
        <img [src]="iconUrl()" 
             class="img-icon" 
             alt="icon" 
             (error)="onError()">
      } 
      @else {
        <mat-icon class="fallback-icon">category</mat-icon>
      }
      
    </div>
  `,
  styleUrl: './category-icon.component.scss'
})
export class CategoryIconComponent {
  iconUrl = input<string | undefined | null>(null);
  type = input<string | undefined | null>('GENERAL');
  hasError = signal(false);

  typeClass = computed(() => {
    const t = this.type()?.toUpperCase();
    switch (t) {
      case 'INCOME': return 'type-income';
      case 'EXPENSE': return 'type-expense';
      case 'GENERAL': return 'type-general';
      default: return 'type-general';
    }
  });

  onError() {
    this.hasError.set(true);
  }
}
