import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TransactionSummary } from '../model/transaction-summary.model';
import { ApiPaths } from '../../../constans/api-paths';
import { TransactionCreateResponse } from '../model/transaction-create-response.model';
import { TransactionCreateRequest } from '../model/transaction-create-request.model';
import { TransactionFilterParams } from '../model/transaction-filter-params.model';
import { PagedResponse } from '../model/paged-response.mode';
import { TransactionUpdateRequest } from '../model/transaction-update-request.model';
import { UpcomingTransactionSummary } from '../model/upcoming-transaction-summary.model';
import { UpcomingTransactionsFilterParams } from '../model/upcoming-transaction-filter-params.mode';
import { TransactionCategoryChangeRequest } from '../model/transaction-category-change-request.model';
import { TransactionCategoryChangeResponse } from '../model/transaction-category-change-response.model';
import { RecurringTemplateSummary } from '../model/recurring-template-summary.model';
import { isAfter } from 'date-fns';
import { RecurringTransactionCreateRequest } from '../model/recurring-template-create-request.model';
import { RecurringTransactionCreateResponse } from '../model/recurring-template-create-response.model';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private http = inject(HttpClient);

  getTransactions(filter: any): Observable<PagedResponse<TransactionSummary>> {
    const params = this.getCleanParams(filter);
    return this.http.get<PagedResponse<TransactionSummary>>(ApiPaths.TRANSACTIONS, {params});
  }

  getUpcomingTransactions(filter: UpcomingTransactionsFilterParams): Observable<PagedResponse<UpcomingTransactionSummary>> {
    const params = this.getCleanParams(filter);
    return this.http.get<PagedResponse<UpcomingTransactionSummary>>(`${ApiPaths.RECURRING_TEMPLATES}/upcoming`, {params});
  }

  getRecurringTemplates(): Observable<PagedResponse<RecurringTemplateSummary>> {
    return this.http.get<PagedResponse<RecurringTemplateSummary>>(ApiPaths.RECURRING_TEMPLATES);
  }

  createTransaction(transactionData: TransactionCreateRequest): Observable<TransactionCreateResponse> {
    return this.http.post<TransactionCreateResponse>(ApiPaths.TRANSACTIONS, transactionData);
  }

  createRecurringTemplate(templateData: RecurringTransactionCreateRequest): Observable<RecurringTransactionCreateResponse> {
    return this.http.post<RecurringTransactionCreateResponse>(ApiPaths.RECURRING_TEMPLATES, templateData);
  }

  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`${ApiPaths.TRANSACTIONS}/${id}`);
  }

  updateTransaction(id: number, data: TransactionUpdateRequest): Observable<void> {
    return this.http.patch<void>(`${ApiPaths.TRANSACTIONS}/${id}`, data);
  }

  changeTransactionCategory(transactionId: number, reqBody: TransactionCategoryChangeRequest): Observable<TransactionCategoryChangeResponse> {
    return this.http.patch<TransactionCategoryChangeResponse>(`${ApiPaths.TRANSACTIONS}/${transactionId}/category`, reqBody);
  }

  changeTemplateStatus(id: number, value: boolean): Observable<void> {
    return this.http.patch<void>(`${ApiPaths.RECURRING_TEMPLATES}/${id}/status`, {isActive: value});
  }

  private getCleanParams(filter: any): HttpParams {
    const cleanedFilter = this.removeNullAndUndefined(filter);
    return new HttpParams({fromObject: cleanedFilter});
  }

  private removeNullAndUndefined(obj: any): any {
    const newObj: any = {};
    Object.keys(obj).forEach(key => {
      const value = obj[key];
      if (value !== null && value !== undefined) {
        newObj[key] = value;
      }
    });
    return newObj;
  }
}
