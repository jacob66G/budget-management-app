import { formatNumber, getCurrencySymbol } from '@angular/common';
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'amountFormat',
  standalone: true
})
export class AmountFormatPipe implements PipeTransform {

  private readonly LOCALE = 'en-US'; 

  transform(
    value: number | null | undefined, 
    currencyCode: string, 

    transactionType?: 'INCOME' | 'EXPENSE' | null 
  ): string {
    
    if (value === null || value === undefined) return '';

    const absValue = Math.abs(value);
    const formattedNum = formatNumber(absValue, this.LOCALE, '1.2-2');

    let symbol = currencyCode;

    if (currencyCode === 'PLN') {
      symbol = 'zł';
    } else {
      try {
        const parts = new Intl.NumberFormat(this.LOCALE, {
          style: 'currency',
          currency: currencyCode,
          currencyDisplay: 'symbol'
        }).formatToParts(0);

        const currencyPart = parts.find(part => part.type === 'currency');
        if (currencyPart) {
          symbol = currencyPart.value;
        }
      } catch (e) {
        symbol = currencyCode;
      }
    }

    let prefix = '';
    
    if (transactionType === 'INCOME') {
      prefix = '+';
    } else if (transactionType === 'EXPENSE') {
      prefix = '-';
    } else {
      prefix = value < 0 ? '-' : ''; 
    }

    if (symbol === undefined || symbol === null) {
      return `${prefix}${formattedNum}`;
    }
    
    return `${prefix}${formattedNum} ${symbol}`;
  }
}
