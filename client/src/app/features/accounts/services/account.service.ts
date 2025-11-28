import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountResponseDto } from '../../../core/models/account-response-dto.model';
import { ApiPaths } from '../../../constans/api-paths';

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  constructor(private http: HttpClient) { }

  getAccounts(): Observable<AccountResponseDto[]> {
    return this.http.get<AccountResponseDto[]>(ApiPaths.ACCOUNTS)
  }
}
