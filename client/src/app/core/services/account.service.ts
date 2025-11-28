import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Account } from "../models/account.model";
import { HttpClient, HttpParams } from "@angular/common/http";
import { ApiPaths } from "../../constans/api-paths";
import { AccountDetails, CreateAccount, SearchCriteria, UpdateAccount } from "../../features/accounts/models/account.model";

@Injectable(
    { 'providedIn': 'root' }
)
export class AccountService {
    http = inject(HttpClient);

    getAccount(id: number): Observable<AccountDetails> {
        return this.http.get<AccountDetails>(`${ApiPaths.Account.ACCOUNT}/${id}`);
    }

    getAccounts(criteria?: SearchCriteria): Observable<Account[]> {
        let params = new HttpParams();

        if (criteria) {
            Object.entries(criteria).forEach(([key, value]) => {
                if (value !== undefined && value !== null) {
                    if (Array.isArray(value)) {
                        if (value.length > 0) {
                            value.forEach(v => {
                                params = params.append(key, v);
                            });
                        }
                    } else {
                        params = params.append(key, value as any);
                    }
                }
            });
        }

        return this.http.get<Account[]>(ApiPaths.Account.ACCOUNT, { params });
    }

    createAccount(createData: CreateAccount): Observable<AccountDetails> {
        return this.http.post<AccountDetails>(ApiPaths.ACCOUNT_BASE, createData);
    }

    updateAccount(id: number, updateData: UpdateAccount): Observable<AccountDetails> {
        return this.http.patch<AccountDetails>(`${ApiPaths.ACCOUNT_BASE}/${id}`, updateData);
    }

    deleteAccount(id: number): Observable<void> {
        return this.http.delete<void>(`${ApiPaths.Account.ACCOUNT}/${id}`);
    }

    activateAccount(id: number): Observable<AccountDetails> {
        return this.http.post<AccountDetails>(`${ApiPaths.Account.ACCOUNT}/${id}/activate`, {});
    }

    deactivateAccount(id: number): Observable<AccountDetails> {
        return this.http.post<AccountDetails>(`${ApiPaths.Account.ACCOUNT}/${id}/deactivate`, {});
    }
}