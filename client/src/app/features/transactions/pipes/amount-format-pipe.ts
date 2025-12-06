import { CurrencyPipe, formatNumber, getCurrencySymbol } from '@angular/common';
import { inject, Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'amountFormat',
  standalone: true
})
export class AmountFormatPipe implements PipeTransform {

  private readonly LOCALE = 'en-US'; 

  transform(
    value: number, 
    currencyCode: string, 
    display: 'code' | 'symbol' | 'symbol-narrow' = 'symbol', 
    isIncome: boolean
  ): string {
    
    if (value === null || value === undefined) return '';

    const formattedNum = formatNumber(Math.abs(value), this.LOCALE, '1.2-2');

    let currencyLabel = currencyCode;

    if (display !== 'code') {
      if (currencyCode === 'PLN') {
        currencyLabel = 'zł';
      } else {
        const format = display === 'symbol-narrow' ? 'narrow' : 'wide';
        currencyLabel = getCurrencySymbol(currencyCode, format);
      }
    }

    let signPrefix = '';
    if (isIncome) {
      signPrefix = '+';
    } else {
      signPrefix = '-';
    }

    return `${signPrefix}${formattedNum} ${currencyLabel}`;
  }
}
