import { Component, input, output } from '@angular/core';
import { Category } from '../../../../core/models/category.model';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatListModule } from "@angular/material/list";
import { CategoryIconComponent } from "../../../../shared/components/category-icon/category-icon.component/category-icon.component";

@Component({
  selector: 'app-category-card',
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatMenuModule, MatListModule, CategoryIconComponent],
  templateUrl: './category-card.component.html',
  styleUrl: './category-card.component.scss'
})
export class CategoryCardComponent {
  category = input.required<Category>();
  
  edit = output<Category>();
  delete = output<Category>();
  reassign = output<Category>();
}
