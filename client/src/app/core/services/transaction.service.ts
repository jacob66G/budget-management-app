import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TransactionSummary } from '../../features/transactions/model/transaction-summary.model';
import { ApiPaths } from '../../constans/api-paths';
import { TransactionCreateResponse } from '../../features/transactions/model/transaction-create-response.model';
import { TransactionCreateRequest } from '../../features/transactions/model/transaction-create-request.model';
import { PagedResponse } from '../../features/transactions/model/paged-response.mode';
import { TransactionUpdateRequest } from '../../features/transactions/model/transaction-update-request.model';
import { UpcomingTransactionSummary } from '../../features/transactions/model/upcoming-transaction-summary.model';
import { UpcomingTransactionsFilterParams } from '../../features/transactions/model/upcoming-transaction-filter-params.mode';
import { TransactionCategoryChangeRequest } from '../../features/transactions/model/transaction-category-change-request.model';
import { TransactionCategoryChangeResponse } from '../../features/transactions/model/transaction-category-change-response.model';
import { RecurringTemplateSummary } from '../../features/transactions/model/recurring-template-summary.model';
import { RecurringTransactionCreateRequest } from '../../features/transactions/model/recurring-template-create-request.model';
import { RecurringTransactionCreateResponse } from '../../features/transactions/model/recurring-template-create-response.model';
import { RecurringTemplateUpdateRequest } from '../../features/transactions/model/recurring-template-update-request.model';
import { UpdateRange } from '../../features/transactions/constants/update-range.enum';
import { RemovalRange } from '../../features/transactions/constants/removal-range.enum';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private http = inject(HttpClient);

  getTransactions(filter: any): Observable<PagedResponse<TransactionSummary>> {
    const params = this.getCleanParams(filter);
    return this.http.get<PagedResponse<TransactionSummary>>(ApiPaths.Transactions.BASE, {params});
  }

  getUpcomingTransactions(filter: UpcomingTransactionsFilterParams): Observable<PagedResponse<UpcomingTransactionSummary>> {
    const params = this.getCleanParams(filter);
    return this.http.get<PagedResponse<UpcomingTransactionSummary>>(`${ApiPaths.RecurringTemplates.UPPCOMMING}`, {params});
  }

  getRecurringTemplates(): Observable<PagedResponse<RecurringTemplateSummary>> {
    return this.http.get<PagedResponse<RecurringTemplateSummary>>(ApiPaths.RecurringTemplates.BASE
    );
  }

  createTransaction(transactionData: TransactionCreateRequest): Observable<TransactionCreateResponse> {
    return this.http.post<TransactionCreateResponse>(ApiPaths.Transactions.BASE, transactionData);
  }

  createRecurringTemplate(templateData: RecurringTransactionCreateRequest): Observable<RecurringTransactionCreateResponse> {
    return this.http.post<RecurringTransactionCreateResponse>(ApiPaths.RecurringTemplates.BASE, templateData);
  }

  updateRecurringTemplate(id: number, request: RecurringTemplateUpdateRequest, updateRange: UpdateRange): Observable<void> {
    const url = ApiPaths.RecurringTemplates.BY_ID(id);
    const params = new HttpParams().set('range', updateRange);
    return this.http.patch<void>(url, request, {params: params});
  }

  deleteRecurringTemplate(id: number, removalRange: RemovalRange): Observable<void> {
    const url = ApiPaths.RecurringTemplates.BY_ID(id);
    const params = new HttpParams().set('range', removalRange);
    return this.http.delete<void>(url, {params: params});
  }

  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`${ApiPaths.Transactions.BY_ID(id)}`);
  }

  updateTransaction(id: number, data: TransactionUpdateRequest): Observable<void> {
    return this.http.patch<void>(`${ApiPaths.Transactions.BY_ID(id)}`, data);
  }

  changeTransactionCategory(transactionId: number, reqBody: TransactionCategoryChangeRequest): Observable<TransactionCategoryChangeResponse> {
    return this.http.patch<TransactionCategoryChangeResponse>(`${ApiPaths.Transactions.BY_ID(transactionId)}/category`, reqBody);
  }

  changeTemplateStatus(id: number, value: boolean): Observable<void> {
    return this.http.patch<void>(`${ApiPaths.RecurringTemplates.STATUS(id)}`, {isActive: value});
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
