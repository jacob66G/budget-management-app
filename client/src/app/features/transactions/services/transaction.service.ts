import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TransactionSummary } from '../model/transaction-summary.model';
import { ApiPaths } from '../../../constans/api-paths';
import { TransactionCreateResponse } from '../model/transaction-create-response.model';
import { TransactionCreateRequest } from '../model/transaction-create-request.model';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private http = inject(HttpClient);

  getTransactions(): Observable<TransactionSummary[]> {
    return this.http.get<TransactionSummary[]>(ApiPaths.TRANSACTIONS);
  }

  createTransaction(transactionData: TransactionCreateRequest): Observable<TransactionCreateResponse> {
    return this.http.post<TransactionCreateResponse>(ApiPaths.TRANSACTIONS, transactionData);
  }
}
