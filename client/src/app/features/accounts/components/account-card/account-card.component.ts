import { Component, inject, input, output } from '@angular/core';
import { Account } from '../../../../core/models/account.model';
import { MatIcon } from "@angular/material/icon";
import { MatCard } from "@angular/material/card";
import { DatePipe, DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-account-card',
  standalone: true,
  imports: [
    MatIcon,
    MatCard,
    DatePipe,
    DecimalPipe
  ],
  templateUrl: './account-card.component.html',
  styleUrl: './account-card.component.scss'
})
export class AccountCardComponent {

  account = input.required<Account>();

  accountClick = output<number>();
  editClick = output<number>();
  deleteClick = output<Account>();

}
